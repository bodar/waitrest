package com.googlecode.waitrest;

import com.googlecode.totallylazy.Strings;
import com.googlecode.utterlyidle.*;
import com.googlecode.utterlyidle.annotations.*;

import static com.googlecode.utterlyidle.Responses.response;

public class Waitress {

    private Kitchen recorder;

    public Waitress(Kitchen recorder) {
        this.recorder = recorder;
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
      return String.valueOf(recorder.countAll());
    }

    @GET
    @Path("{path:.*}")
    @Priority(Priority.Low)
    public Response serveOrder(Request request) {
        return recorder.serve(request).getOrElse(response(Status.NOT_FOUND).header("Content-Type", request.headers().getValue("Content-Type")));
    }

    @POST
    @Path("order")
    @Priority(Priority.High)
    public Response takeOrder(@FormParam("request") String req, @FormParam("response") String resp) {
        Request request = parseRequest(req);
        Response response = response(Status.OK).entity(resp);

        recorder.receiveOrder(request, response);

        return response(Status.CREATED).
                header(HttpHeaders.LOCATION, request.url().toString()).entity(html(request.toString(), response.toString()));
    }

    @POST
    @Path("{path:.*}")
    @Priority(Priority.Low)
    public Response serveOrder(@FormParam("request") String request) {
        return recorder.serve(parseRequest(request)).getOrElse(response(Status.NOT_FOUND));
    }

    private Request parseRequest(String req) {
        return new RequestParser().parse(req).build();
    }

    private String html(String request, String response) {
        return String.format("<html><head><title>Created</title></head><body><p>%s</p><p>%s</p></body></html>", request, response);
    }
}
