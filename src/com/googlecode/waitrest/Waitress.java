package com.googlecode.waitrest;

import com.googlecode.utterlyidle.HttpMessageParser;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.*;
import com.googlecode.utterlyidle.io.Url;

import java.util.Map;

import static com.googlecode.utterlyidle.HttpHeaders.LOCATION;
import static com.googlecode.utterlyidle.Responses.response;
import static com.googlecode.utterlyidle.Status.CREATED;
import static com.googlecode.utterlyidle.Status.NOT_FOUND;
import static com.googlecode.utterlyidle.proxy.Resource.redirect;
import static com.googlecode.utterlyidle.proxy.Resource.resource;

public class Waitress {

    public static final String WAITRESS_ORDER_PATH = "/order";
    public static final String WAITRESS_ORDERS_PATH = "/orders";
    private static final String ANY_PATH_EXCEPT_RESERVED_PATH = "{path:[^" + WAITRESS_ORDERS_PATH + "].*}";

    private Kitchen kitchen;

    public Waitress(Kitchen kitchen) {
        this.kitchen = kitchen;
    }

    @GET
    @Path(WAITRESS_ORDER_PATH)
    @Priority(Priority.High)
    @Produces("text/html")
    public String showMenu() {
        return "OK";
    }

    @GET
    @Path(WAITRESS_ORDERS_PATH)
    @Produces("text/plain")
    @Priority(Priority.High)
    public Map<Request, Response> allOrders() {
        return kitchen.allOrders();
    }

    @GET
    @Path(ANY_PATH_EXCEPT_RESERVED_PATH)
    @Priority(Priority.Low)
    public Response serveOrder(Request request) {
        return kitchen.serve(request).getOrElse(response(NOT_FOUND));
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
            return redirect(resource(Waitress.class).showMenu());
        }
    }

    @POST
    @Path("{path:.*}")
    @Priority(Priority.Low)
    public Response serveOrder(@FormParam("request") String request) {
        return kitchen.serve(HttpMessageParser.parseRequest(request)).getOrElse(response(NOT_FOUND));
    }

    @PUT
    @Path("{path:.*}")
    public Response takeOrder(Request request) {
        kitchen.receiveOrder(request);
        return created(request);
    }

    private Response created(Request request) {
        return response(CREATED).
                header(LOCATION, request.url().toString()).entity(html(request.url()));
    }

    private String html(Url url) {
        return String.format("<html><head><title>Created</title></head><body><a href='%1$s'>%1$s</a></body></html>", url);
    }
}
