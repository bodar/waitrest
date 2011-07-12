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
import static com.googlecode.utterlyidle.RequestBuilder.*;
import static com.googlecode.utterlyidle.Responses.response;
import static com.googlecode.utterlyidle.Status.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
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
        String cheeseUrl = server.getUrl() + "cheese";
        Request put = put(cheeseUrl).withQuery("type", "cheddar").withHeader(CONTENT_TYPE, TEXT_PLAIN).withHeader(CONTENT_LENGTH, "6").withInput("cheese".getBytes()).build();
        Request get = get(cheeseUrl).withQuery("type", "cheddar").build();
        Request unknownGet = get(cheeseUrl).withQuery("type", "gouda").build();
        Response response = response(OK).bytes("cheese".getBytes()).header(CONTENT_TYPE, TEXT_PLAIN).header(CONTENT_LENGTH, "6");

        restClient.handle(put);

        assertThat(restClient.handle(get), is(response));
        assertThat(restClient.handle(unknownGet).status(), is(Status.NOT_FOUND));
    }

    @Test
    public void serveRequestResponseOrder_get() throws Exception {
        String cheeseUrl = server.getUrl() + "cheese";
        Request request = get(cheeseUrl).withQuery("type", "cheddar").build();
        Request unknownRequest = get(cheeseUrl).withQuery("type", "gouda").build();
        Response expectedResponse = response(OK).bytes("cheese".getBytes()).header(CONTENT_TYPE, TEXT_PLAIN).header(CONTENT_LENGTH, "6");

        restClient.handle(post(server.getUrl() + Waitress.WAITRESS_ORDER_PATH).withForm("request", request.toString()).withForm("response", expectedResponse.toString()).build());

        assertThat(restClient.handle(request), is(expectedResponse));
        assertThat(restClient.handle(unknownRequest).status(), is(Status.NOT_FOUND));
    }

    @Test
    public void serveRequestResponseOrder_post() throws Exception {
        String cheeseUrl = server.getUrl() + "cheese";
        Request request = post(cheeseUrl).withForm("type", "cheddar").build();
        Request unknownRequest = post(cheeseUrl).withForm("type", "gouda").build();
        Response expectedResponse = response(OK).bytes("cheese".getBytes()).header(CONTENT_TYPE, TEXT_PLAIN).header(CONTENT_LENGTH, "6");

        restClient.handle(post(server.getUrl() + Waitress.WAITRESS_ORDER_PATH).withForm("request", request.toString()).withForm("response", expectedResponse.toString()).build());

        assertThat(restClient.handle(request), is(expectedResponse));
        assertThat(restClient.handle(unknownRequest).status(), is(Status.NOT_FOUND));
    }

    @Test
    public void cannotTakeInvalidOrder() throws Exception {
        Response response = restClient.handle(post(server.getUrl() + Waitress.WAITRESS_ORDER_PATH).
                withForm("request", "/pathWithNoHttpMethod HTTP/1.1").
                withForm("response", response(NO_CONTENT).toString()).build());

        assertThat(response.status(), is(BAD_REQUEST));
        assertThat(response.toString(), containsString("Request without a valid method"));
    }
}
