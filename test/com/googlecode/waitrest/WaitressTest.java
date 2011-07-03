package com.googlecode.waitrest;

import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import org.junit.Test;

import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.MediaType.TEXT_PLAIN;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.RequestBuilder.put;
import static com.googlecode.utterlyidle.Responses.response;
import static com.googlecode.utterlyidle.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class WaitressTest {
    private Kitchen kitchen = new Kitchen();
    private Waitress waitress = new Waitress(kitchen);

    @Test
    public void serveRequestResponseOrder() {
        Request request = get("/cheese").build();
        Response response = response(OK).bytes("cheese".getBytes());

        waitress.takeOrder(request.toString(), response.toString());

        assertThat(waitress.serveOrder(request.toString()).toString(), is(response.toString()));
    }

    @Test
    public void serveRequestResponseOrderWithQueryParams() {
        Request requestWithQueryParam = get("/cheese").withQuery("type", "cheddar").build();
        Request requestWithoutQueryParam = get("/cheese").build();
        Response response = response(OK).bytes("cheddar".getBytes());

        waitress.takeOrder(requestWithQueryParam.toString(), response.toString());

        assertThat(waitress.serveOrder(requestWithQueryParam.toString()).toString(), is(response.toString()));
        assertThat(waitress.serveOrder(requestWithoutQueryParam.toString()).status(), is(Status.NOT_FOUND));
    }

    @Test
    public void serveRequestOrder() {
        waitress.takeOrder(put("/cheese").withHeader(CONTENT_TYPE, TEXT_PLAIN).withInput("cheese".getBytes()).build());

        assertThat(waitress.serveOrder(get("/cheese").build()).toString(), is(response(OK).header(CONTENT_TYPE, TEXT_PLAIN).bytes("cheese".getBytes()).toString()));
    }
}