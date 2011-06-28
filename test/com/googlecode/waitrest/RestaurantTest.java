package com.googlecode.waitrest;

import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.ServerConfiguration;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.httpserver.RestServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_LENGTH;
import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.MediaType.TEXT_PLAIN;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.RequestBuilder.post;
import static com.googlecode.utterlyidle.RequestBuilder.put;
import static com.googlecode.utterlyidle.Responses.response;
import static com.googlecode.utterlyidle.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RestaurantTest {

    private RestServer server;
    private ClientHttpHandler restClient = new ClientHttpHandler();

    @Before
    public void setUp() throws Exception {
        server = new RestServer(new Restaurant(), ServerConfiguration.defaultConfiguration().port(8003));
    }

    @After
    public void tearDown() throws IOException {
        server.close();
    }

    @Test
    public void serveRequestOrder() throws Exception {
        Request put = put(server.getUrl() + "cheese").withQuery("type", "cheddar").withHeader(CONTENT_TYPE, TEXT_PLAIN).withHeader(CONTENT_LENGTH, "6").withInput("cheese".getBytes()).build();
        Request get = get(server.getUrl() + "cheese").withQuery("type", "cheddar").build();
        Request unknownGet = get(server.getUrl() + "cheese").withQuery("type", "gouda").build();
        Response response = response(OK).bytes("cheese".getBytes()).header(CONTENT_TYPE, TEXT_PLAIN).header(CONTENT_LENGTH, "6");

        restClient.handle(put);

        assertThat(restClient.handle(get), is(response));
        assertThat(restClient.handle(unknownGet).status(), is(Status.NOT_FOUND));
    }

    @Test
    public void serveRequestResponseOrder() throws Exception {
        Request request = get(server.getUrl() + "cheese").withQuery("type", "cheddar").build();
        Request unknownRequest = get(server.getUrl() + "cheese").withQuery("type", "gouda").build();
        Response expectedResponse = response(OK).bytes("cheese".getBytes()).header(CONTENT_TYPE, TEXT_PLAIN).header(CONTENT_LENGTH, "6");

        restClient.handle(post(server.getUrl() + "order").withForm("request", request.toString()).withForm("response", expectedResponse.toString()).build());

        assertThat(restClient.handle(request), is(expectedResponse));
        assertThat(restClient.handle(unknownRequest).status(), is(Status.NOT_FOUND));
    }
}
