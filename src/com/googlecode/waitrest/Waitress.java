package com.googlecode.waitrest;


import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Pair;
import com.googlecode.utterlyidle.HttpMessageParser;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.annotations.*;
import com.googlecode.utterlyidle.io.Url;

import java.util.Collections;
import java.util.Map;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.utterlyidle.HttpHeaders.LOCATION;
import static com.googlecode.utterlyidle.Responses.response;
import static com.googlecode.utterlyidle.Status.CREATED;
import static com.googlecode.utterlyidle.Status.NOT_FOUND;
import static com.googlecode.utterlyidle.proxy.Resource.redirect;
import static com.googlecode.utterlyidle.proxy.Resource.resource;

public class Waitress {

    public static final String WAITRESS_ORDER_PATH = "/waitrest/order";
    public static final String WAITRESS_ORDERS_PATH = "/waitrest/orders";
    private static final String ANY_PATH = "{path:^(?!waitrest/order|waitrest/orders).*}";

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
    public Map<String, String> showMenu() {
        return model(Pair.<String, String>pair("orderUrl", WAITRESS_ORDER_PATH),
                     Pair.<String, String>pair("ordersUrl", WAITRESS_ORDERS_PATH));
    }

    @GET
    @Path(WAITRESS_ORDERS_PATH)
    @Produces("text/plain")
    @Priority(Priority.High)
    public Map<Request, Response> allOrders() {
        return kitchen.allOrders();
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
            return response(Status.BAD_REQUEST).entity(model(Pair.<String, String>pair("error", e.getMessage()),
                                                                Pair.<String, String>pair("orderUrl", WAITRESS_ORDER_PATH),
                                                                Pair.<String, String>pair("ordersUrl", WAITRESS_ORDERS_PATH),
                                                                Pair.<String, String>pair("request", req),
                                                                Pair.<String, String>pair("response", resp)));
        }
    }

    private Map<String, String> model(Pair<String, String>... elements) {
        return sequence(elements).fold(Maps.<String, String>map(), Maps.<String, String>asMap());
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

    private Response created(Request request) {
        return response(CREATED).
                header(LOCATION, request.url().toString()).entity(model(Pair.<String, String>pair("url", request.url().toString()),
                                                                        Pair.<String, String>pair("method", request.method())));
    }
}
