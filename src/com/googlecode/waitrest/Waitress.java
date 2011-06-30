package com.googlecode.waitrest;

import com.googlecode.totallylazy.Strings;
import com.googlecode.utterlyidle.*;
import com.googlecode.utterlyidle.annotations.*;
import com.googlecode.utterlyidle.io.Url;

import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.HttpHeaders.LOCATION;
import static com.googlecode.utterlyidle.Responses.response;
import static com.googlecode.utterlyidle.Status.CREATED;

public class Waitress {

    public static final String WAITRESS_ORDER_PATH = "/order";

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
    @Path("orders")
    @Produces("text/html")
    @Priority(Priority.High)
    public String countAll() {
        return String.valueOf(kitchen.countAll());
    }

    @GET
    @Path("{path:.*}")
    @Priority(Priority.Low)
    public Response serveOrder(Request request) {
        return kitchen.serve(request).getOrElse(response(Status.NOT_FOUND));
    }

    @POST
    @Path(WAITRESS_ORDER_PATH)
    @Priority(Priority.High)
    public Response takeOrder(@FormParam("request") String req, @FormParam("response") String resp) {
        Request request = HttpMessageParser.parseRequest(req);
        Response response = HttpMessageParser.parseResponse(resp);

        kitchen.receiveOrder(request, response);

        return created(request);
    }

    @POST
    @Path("{path:.*}")
    @Priority(Priority.Low)
    public Response serveOrder(@FormParam("request") String request) {
        return kitchen.serve(HttpMessageParser.parseRequest(request)).getOrElse(response(Status.NOT_FOUND));
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
