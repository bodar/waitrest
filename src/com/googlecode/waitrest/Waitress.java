package com.googlecode.waitrest;


import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.HttpMessageParser;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Requests;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.HttpMethod;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.PUT;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Priority;
import com.googlecode.utterlyidle.annotations.Produces;

import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.utterlyidle.HttpHeaders.LOCATION;
import static com.googlecode.utterlyidle.Responses.response;
import static com.googlecode.utterlyidle.Status.CREATED;
import static com.googlecode.utterlyidle.Status.NOT_FOUND;
import static com.googlecode.utterlyidle.proxy.Resource.redirect;
import static com.googlecode.utterlyidle.proxy.Resource.resource;

public class Waitress {

    public static final String WAITRESS_ORDER_PATH = "waitrest/order";
    public static final String WAITRESS_ORDERS_PATH = "waitrest/orders";
    public static final String WAITRESS_GET_ORDERS_PATH = "waitrest/orders/get";
    private static final String ANY_PATH = "{path:^(?!waitrest/order|waitrest/orders|waitrest/orders/get).*}";

    private Kitchen kitchen;

    public Waitress(Kitchen kitchen) {
        this.kitchen = kitchen;
    }

    @GET
    @Path("")
    @Priority(Priority.High)
    public Response root() {
        return redirect(resource(Waitress.class).showMenu());
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
    public Response allOrders() {
        return response(Status.OK).entity(model().add("orders", kitchen.allOrders()));
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
        try {
            Request request = HttpMessageParser.parseRequest(req);
            Response response = HttpMessageParser.parseResponse(resp);

            kitchen.receiveOrder(request, response);

            return created(request);
        } catch (IllegalArgumentException e) {
            return response(Status.BAD_REQUEST).entity(model().
                    add("error", e.getMessage()).
                    add("orderUrl", absolute(WAITRESS_ORDER_PATH)).
                    add("ordersUrl", absolute(WAITRESS_ORDERS_PATH)).
                    add("getOrdersUrl", absolute(WAITRESS_GET_ORDERS_PATH)).
                    add("request", req).
                    add("response", resp));
        }
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

    private Response created(Request request) {
        Model model = model().
                add("url", request.uri().toString()).
                add("method", request.method());
        if (request.method().equalsIgnoreCase(HttpMethod.POST))
            model.add("formParameters", sequence(Requests.form(request)).map(first(String.class)).toList());

        return response(CREATED).header(LOCATION, request.uri().toString()).entity(model);
    }


}
