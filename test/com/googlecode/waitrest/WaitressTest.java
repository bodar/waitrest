package com.googlecode.waitrest;

import com.googlecode.utterlyidle.*;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class WaitressTest {
    private Kitchen kitchen = new Kitchen();
    private Waitress waitress = new Waitress(kitchen);

    @Test
    public void serveTakenOrder() {
        Request request = RequestBuilder.get("/cheese").build();
        Response response = Responses.response(Status.OK).bytes("cheese".getBytes());

        waitress.takeOrder(request.toString(), response.toString());

        assertThat(response.toString(), is(waitress.serveOrder(request.toString()).toString()));
    }
}
