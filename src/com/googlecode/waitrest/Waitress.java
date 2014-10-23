package com.googlecode.waitrest;


import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.numbers.Numbers;
import com.googlecode.utterlyidle.*;
import com.googlecode.utterlyidle.annotations.*;

import java.io.File;
import java.util.Map;

import static com.googlecode.funclate.Model.persistent.model;
import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.HttpHeaders.LOCATION;
import static com.googlecode.utterlyidle.HttpMessageParser.parseRequest;
import static com.googlecode.utterlyidle.HttpMessageParser.parseResponse;
import static com.googlecode.utterlyidle.MediaType.APPLICATION_JSON;
import static com.googlecode.utterlyidle.MediaType.TEXT_HTML;
import static com.googlecode.utterlyidle.MediaType.TEXT_PLAIN;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.ResponseBuilder.response;
import static com.googlecode.utterlyidle.Status.CREATED;
import static com.googlecode.utterlyidle.Status.NOT_FOUND;
import static com.googlecode.waitrest.Kitchen.prepareOrder;

public class Waitress {

    public static final String REQUEST_SEPARATOR = "---------- REQUEST ------------";
    public static final String RESPONSE_SEPARATOR = "---------- RESPONSE -----------";
    public static final String WAITRESS_ORDER_PATH = "waitrest/order";
    public static final String WAITRESS_IMPORT_PATH = "waitrest/import";
    public static final String WAITRESS_IMPORT_FROM_FILE_PATH = "waitrest/importFromFile";
    public static final String WAITRESS_ORDERS_PATH = "waitrest/orders";
    public static final String WAITRESS_GET_ORDERS_PATH = "waitrest/orders/get";
    public static final String WAITRESS_DELETE_ORDERS_PATH = "waitrest/orders/delete";
    public static final String WAITRESS_ORDER_COUNTS_PATH = "waitrest/orders/counts";
    private static final String ANY_PATH = "{path:^(?!waitrest/order|waitrest/orders|waitrest/orders/get|waitrest/export|waitrest/import).*}";

    private final Kitchen kitchen;
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
    @Produces(TEXT_HTML)
    public Model showMenu() {
        return model().add("orderUrl", absolute(WAITRESS_ORDER_PATH)).
                add("ordersUrl", absolute(WAITRESS_ORDERS_PATH)).
                add("getOrdersUrl", absolute(WAITRESS_GET_ORDERS_PATH)).
                add("deleteOrdersUrl", absolute(WAITRESS_DELETE_ORDERS_PATH)).
                add("importOrdersUrl", absolute(WAITRESS_IMPORT_PATH)).
                add("importOrdersFromFileUrl", absolute(WAITRESS_IMPORT_FROM_FILE_PATH)).
                add("getOrderCounts", absolute(WAITRESS_ORDER_COUNTS_PATH));
    }

    @GET
    @Path(WAITRESS_ORDERS_PATH)
    @Produces(TEXT_PLAIN)
    @Priority(Priority.High)
    public Response allOrders() {
        Map<Request, Response> orders = kitchen.allOrdersInMemory();
        return response(Status.OK).entity(model().add("orders", orders).add("requestSeparator", REQUEST_SEPARATOR).add("responseSeparator", RESPONSE_SEPARATOR)).build();
    }

    @POST
    @Path(WAITRESS_IMPORT_PATH)
    @Produces(TEXT_PLAIN)
    @Priority(Priority.High)
    public String importOrders(@FormParam("orders") String orders) {
        Sequence<String> messages = sequence(orders.split(REQUEST_SEPARATOR)).filter(not(Strings.blank()));
        messages.map(prepareOrder()).forEach(placeOrder());
        return messages.size() + " orders imported";
    }

    @POST
    @Path(WAITRESS_IMPORT_FROM_FILE_PATH)
    @Produces(TEXT_PLAIN)
    public String importOrdersFromFile(@FormParam("importFile") String importFile) throws Exception {
        final long start = System.currentTimeMillis();
        File file = new File(importFile);

        final Sequence<File> files = file.isFile() ? sequence(file) : Files.recursiveFiles(file).filter(Files.isFile()).filter(Files.hasSuffix("txt"));

        final Number counter = files.mapConcurrently(new Callable1<File, Number>() {
            @Override
            public Number call(File file) throws Exception {
                return kitchen.takeOrdersFrom(file);
            }
        }).fold(0, Numbers.add());
        return counter + " orders imported in " +  ((System.currentTimeMillis() - start) / 1000) + " sec";
    }

    @GET
    @Path(WAITRESS_GET_ORDERS_PATH)
    @Produces(TEXT_HTML)
    @Priority(Priority.High)
    public Response allGetOrders() {
        Sequence<String> urls = sequence(kitchen.allOrdersInMemory(HttpMethod.GET).keySet()).map(uri()).map(Callables.<Uri>asString());
        return response(Status.OK).entity(model().add("urls", urls.toList())).build();
    }

    @GET
    @Path(WAITRESS_ORDER_COUNTS_PATH)
    @Priority(Priority.High)
    @Produces({TEXT_HTML, APPLICATION_JSON})
    public Model serveGetOrderCounts() {
        final int ordersInMemoryCount = kitchen.allOrdersInMemory().size();
        final Sequence<Model> importedOrderCounts = kitchen.importedOrderCounts().map(new Mapper<Pair<String, Integer>, Model>() {
            @Override
            public Model call(Pair<String, Integer> ordersCount) throws Exception {
                return model().add("filePath", ordersCount.first()).add("count", ordersCount.second());
            }
        });
        final Number totalImportedOrdersCount = importedOrderCounts.map(Model.functions.value("count", Integer.class)).fold(0, Numbers.add());
        return model().add("inMemoryCount", ordersInMemoryCount).add("importedOrderCounts", importedOrderCounts).add("totalImportedOrderCount", totalImportedOrdersCount);
    }

    @GET
    @Path(ANY_PATH)
    @Priority(Priority.Low)
    public Response serveGetOrder(Request request) {
        return kitchen.serve(request).getOrElse(response(NOT_FOUND).entity("Order not found").build());
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
                return response(Status.CREATED).entity(menuPageBaseModel(req, resp).add("message", "Quick order taken for " + request.method() + " " + request.uri())).build();
            }
            return created(request);
        } catch (IllegalArgumentException e) {
            return response(Status.BAD_REQUEST).entity(menuPageBaseModel(req, resp).add("error", e.getMessage())).build();
        }
    }

    @POST
    @Path(ANY_PATH)
    @Priority(Priority.Low)
    public Response servePostOrder(Request request) {
        return kitchen.serve(request).getOrElse(response(NOT_FOUND).build());
    }

    @PUT
    @Path(ANY_PATH)
    public Response takeOrder(Request put) {
        kitchen.receiveOrder(get(put.uri()).build(), response().header(CONTENT_TYPE, put.headers().getValue(CONTENT_TYPE)).
                entity(put.entity().asBytes()).build());
        return created(put);
    }

    @POST
    @Path(WAITRESS_DELETE_ORDERS_PATH)
    @Priority(Priority.High)
    public Response deleteAllOrders() {
        kitchen.deleteAllOrders();
        return redirector.seeOther(method(on(Waitress.class).allOrders()));
    }

    private Model menuPageBaseModel(String req, String resp) {
        return model().add("orderUrl", absolute(WAITRESS_ORDER_PATH)).
                add("ordersUrl", absolute(WAITRESS_ORDERS_PATH)).
                add("getOrdersUrl", absolute(WAITRESS_GET_ORDERS_PATH)).
                add("request", req).
                add("response", resp);
    }

    private Block<Pair<Request, Response>> placeOrder() {
        return new Block<Pair<Request, Response>>() {
            @Override
            public void execute(Pair<Request, Response> requestAndResponse) throws Exception {
                kitchen.receiveOrder(requestAndResponse.first(), requestAndResponse.second());
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
            model = model.add("formParameters", sequence(Requests.form(request)).map(first(String.class)).toList());

        return response(CREATED).header(LOCATION, request.uri().toString()).entity(model).build();
    }


}
