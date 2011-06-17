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

    private Kitchen kitchen;

    public Waitress(Kitchen kitchen) {
        this.kitchen = kitchen;
    }

    @GET
    @Path("order")
    @Priority(Priority.High)
    @Produces("text/html")
    public String showMenu() {
        return Strings.toString(getClass().getResourceAsStream("menu.html"));
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
        return kitchen.serve(request).getOrElse(response(Status.NOT_FOUND).header(CONTENT_TYPE, request.headers().getValue(CONTENT_TYPE)));
    }

    @POST
    @Path("order")
    @Priority(Priority.High)
    public Response takeOrder(@FormParam("request") String req, @FormParam("response") String resp) {
        Request request = parseRequest(req);
        Response response = response(Status.OK).entity(resp);

        kitchen.receiveOrder(request, response);

        return created(request);
    }

    @POST
    @Path("{path:.*}")
    @Priority(Priority.Low)
    public Response serveOrder(@FormParam("request") String request) {
        return kitchen.serve(parseRequest(request)).getOrElse(response(Status.NOT_FOUND));
    }

    @PUT
    @Path("{path:.*}")
    public Response put(Request request) {
        kitchen.receiveOrder(request);
        return created(request);
    }

    private Request parseRequest(String req) {
        return new RequestParser().parse(req).build();
    }

    private Response created(Request request) {
        return response(CREATED).
                header(LOCATION, request.url().toString()).entity(html(request.url()));
    }

    private String html(Url url) {
        return String.format("<html><head><title>Created</title></head><body><a href='%1$s'>'%1$s'</a></body></html>", url);
    }
}
