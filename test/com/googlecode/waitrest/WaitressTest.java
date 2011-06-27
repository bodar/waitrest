package com.googlecode.waitrest;

import com.googlecode.utterlyidle.*;
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

        assertThat(response, is(waitress.serveOrder(request.toString())));
    }

    @Test
    public void serveRequestOrder() {
        waitress.takeOrder(put("/cheese").withHeader(CONTENT_TYPE, TEXT_PLAIN).withInput("cheese".getBytes()).build());

        assertThat(response(OK).header(CONTENT_TYPE, TEXT_PLAIN).bytes("cheese".getBytes()), is(waitress.serveOrder(get("/cheese").build())));

    }
}
