package com.googlecode.waitrest;

import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.ServerConfiguration;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.httpserver.RestServer;
import org.junit.Test;

import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_LENGTH;
import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.MediaType.TEXT_PLAIN;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.RequestBuilder.put;
import static com.googlecode.utterlyidle.Responses.response;
import static com.googlecode.utterlyidle.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RestaurantTest {
    @Test
    public void serveRequestOrderWithRealServer() throws Exception {
        RestServer server = new RestServer(new Restaurant(), ServerConfiguration.defaultConfiguration().port(8001));
        Request request = get(server.getUrl() + "cheese").build();
        Response expectedResponse = response(OK).bytes("cheese".getBytes()).header(CONTENT_TYPE, TEXT_PLAIN).header(CONTENT_LENGTH, "6");

        ClientHttpHandler restClient = new ClientHttpHandler();

        restClient.handle(put(server.getUrl() + "cheese").withHeader(CONTENT_TYPE, TEXT_PLAIN).withHeader(CONTENT_LENGTH, "6").withInput("cheese".getBytes()).build());

        assertThat(restClient.handle(request), is(expectedResponse));
    }
}
