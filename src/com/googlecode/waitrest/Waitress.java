package com.googlecode.waitrest;


import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.*;
import com.googlecode.utterlyidle.annotations.*;

import java.util.Map;

import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.HttpHeaders.LOCATION;
import static com.googlecode.utterlyidle.HttpMessageParser.parseRequest;
import static com.googlecode.utterlyidle.HttpMessageParser.parseResponse;
import static com.googlecode.utterlyidle.Responses.response;
import static com.googlecode.utterlyidle.Status.CREATED;
import static com.googlecode.utterlyidle.Status.NOT_FOUND;

public class Waitress {

    public static final String REQUEST_SEPARATOR = "---------- REQUEST ------------";
    public static final String RESPONSE_SEPARATOR = "---------- RESPONSE -----------";
    public static final String WAITRESS_ORDER_PATH = "waitrest/order";
    public static final String WAITRESS_IMPORT_PATH = "waitrest/import";
    public static final String WAITRESS_ORDERS_PATH = "waitrest/orders";
    public static final String WAITRESS_GET_ORDERS_PATH = "waitrest/orders/get";
    private static final String ANY_PATH = "{path:^(?!waitrest/order|waitrest/orders|waitrest/orders/get|waitrest/export|waitrest/import).*}";

    private Kitchen kitchen;
    private final Redirector redirector;

    public Waitress(Kitchen kitchen, Redirector redirector) {
        this.kitchen = kitchen;
        this.redirector = redirector;
    }

    @GET
    @Path("")
    @Priority(Priority.High)
    public Response root() {
        return redirector.seeOther(method(on(Waitress.class).showMenu()));
    }

    @GET
    @Path(WAITRESS_ORDER_PATH)
    @Priority(Priority.High)
    @Produces("text/html")
    public Model showMenu() {
        return model().add("orderUrl", absolute(WAITRESS_ORDER_PATH)).add("ordersUrl", absolute(WAITRESS_ORDERS_PATH)).add("getOrdersUrl", absolute(WAITRESS_GET_ORDERS_PATH));
    }

    @GET
    @Path(WAITRESS_ORDERS_PATH)
    @Produces("text/plain")
    @Priority(Priority.High)
    public Response allOrders(@QueryParam("authority") final Option<String> authority) {
        Map<Request, Response> orders = authority.isEmpty() ? kitchen.allOrders() : Maps.map(Maps.pairs(kitchen.allOrders()).map(authorityToNewAuthority(authority.get())));
        return response(Status.OK).entity(model().add("orders", orders).add("requestSeparator", REQUEST_SEPARATOR).add("responseSeparator", RESPONSE_SEPARATOR));
    }

    private Callable1<Pair<Request, Response>, Pair<Request, Response>> authorityToNewAuthority(final String newAuthority) {
        return new Callable1<Pair<Request, Response>, Pair<Request, Response>>() {
            @Override
            public Pair<Request, Response> call(Pair<Request, Response> order) throws Exception {
                Request request = order.first();
                Response response = order.second();
                Request replacedRequest = request.uri(request.uri().authority(newAuthority));
                Response replacedResponse = response.bytes(new String(response.bytes()).replaceAll(request.uri().authority(), newAuthority).getBytes());
                return Pair.pair(replacedRequest, replacedResponse);
            }
        };
    }

    @POST
    @Path(WAITRESS_IMPORT_PATH)
    @Produces("text/plain")
    @Priority(Priority.High)
    public String importOrders(@FormParam("orders") String orders) {
        Sequence<String> messages = sequence(orders.trim().split(REQUEST_SEPARATOR)).filter(not(Strings.empty()));
        messages.forEach(takeOrder());
        return messages.size()+" orders imported";
    }

    @GET
    @Path(WAITRESS_GET_ORDERS_PATH)
    @Produces("text/html")
    @Priority(Priority.High)
    public Response allGetOrders() {
        Sequence<String> urls = sequence(kitchen.allOrders(HttpMethod.GET).keySet()).map(uri()).map(Callables.<Uri>asString());
        return response(Status.OK).entity(model().add("urls", urls.toList()));
    }

    @GET
    @Path(ANY_PATH)
    @Priority(Priority.Low)
    public Response serveGetOrder(Request request) {
        return kitchen.serve(request).getOrElse(response(NOT_FOUND).entity("Order not found"));
    }

    @POST
    @Path(WAITRESS_ORDER_PATH)
    @Priority(Priority.High)
    public Response takeOrder(@FormParam("request") String req, @FormParam("response") String resp) {
        return takeOrder(req, resp, Option.none(String.class));
    }

    @POST
    @Path(WAITRESS_ORDER_PATH)
    @Priority(Priority.High)
    public Response takeOrder(@FormParam("request") String req, @FormParam("response") String resp, @FormParam("action") Option<String> action) {
        try {
            Request request = parseRequest(req);
            Response response = parseResponse(resp);

            kitchen.receiveOrder(request, response);

            if (action.getOrElse("").contains("quick")) {
                return response(Status.CREATED).entity(menuPageBaseModel(req, resp).add("message", "Quick order taken for " + request.method() + " " + request.uri()));
            }
            return created(request);
        } catch (IllegalArgumentException e) {
            return response(Status.BAD_REQUEST).entity(menuPageBaseModel(req, resp).add("error", e.getMessage()));
        }
    }

    private Model menuPageBaseModel(String req, String resp) {
        return model().add("orderUrl", absolute(WAITRESS_ORDER_PATH)).
                       add("ordersUrl", absolute(WAITRESS_ORDERS_PATH)).
                       add("getOrdersUrl", absolute(WAITRESS_GET_ORDERS_PATH)).
                       add("request", req).
                       add("response", resp);
    }

    @POST
    @Path(ANY_PATH)
    @Priority(Priority.Low)
    public Response servePostOrder(Request request) {
        return kitchen.serve(request).getOrElse(response(NOT_FOUND));
    }

    @PUT
    @Path(ANY_PATH)
    public Response takeOrder(Request request) {
        kitchen.receiveOrder(request);
        return created(request);
    }

    private Callable1<String, Void> takeOrder() {
        return new Callable1<String, Void>() {
            @Override
            public Void call(String requestAndResponse) throws Exception {
                Sequence<String> requestAndResponseSequence = sequence(requestAndResponse.trim().split(RESPONSE_SEPARATOR));
                kitchen.receiveOrder(parseRequest(requestAndResponseSequence.first().trim()), parseResponse(requestAndResponseSequence.second().trim()));
                return null;
            }
        };
    }

    public static Callable1<Request, Uri> uri() {
        return new Callable1<Request, Uri>() {
            public Uri call(Request request) throws Exception {
                return request.uri();
            }
        };
    }

    private String absolute(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }

    private Callable1<? super Pair<Request, Response>, Pair<String, String>> asJsonString() {
        return new Callable1<Pair<Request, Response>, Pair<String, String>>() {
            @Override
            public Pair<String, String> call(Pair<Request, Response> requestResponsePair) throws Exception {
                return pair((requestResponsePair.first().toString()), requestResponsePair.second().toString());
            }
        };
    }

    private Response created(Request request) {
        Model model = model().
                add("url", request.uri().toString()).
                add("method", request.method());
        if (request.method().equalsIgnoreCase(HttpMethod.POST))
            model.add("formParameters", sequence(Requests.form(request)).map(first(String.class)).toList());

        return response(CREATED).header(LOCATION, request.uri().toString()).entity(model);
    }


}
