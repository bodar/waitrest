package com.googlecode.waitrest;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Strings;
import com.googlecode.utterlyidle.BaseUri;
import com.googlecode.utterlyidle.BaseUriRedirector;
import com.googlecode.utterlyidle.Binding;
import com.googlecode.utterlyidle.Bindings;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import org.junit.Test;

import java.util.Iterator;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Some.some;
import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.MediaType.TEXT_PLAIN;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.RequestBuilder.put;
import static com.googlecode.utterlyidle.Responses.response;
import static com.googlecode.utterlyidle.Status.OK;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;

public class WaitressTest {
    private Kitchen kitchen = new Kitchen();
    private Waitress waitress = new Waitress(kitchen, null);

    @Test
    public void serveRequestResponseOrder() {
        Request request = get("/cheese").build();
        Response response = response(OK).bytes("cheese".getBytes());

        waitress.takeOrder(request.toString(), response.toString());

        assertThat(waitress.serveGetOrder(request).toString(), is(response.toString()));
    }

    @Test
    public void serveRequestResponseOrderWithQueryParams() {
        Request requestWithQueryParam = get("/cheese").withQuery("type", "cheddar").build();
        Request requestWithoutQueryParam = get("/cheese").build();
        Response response = response(OK).bytes("cheddar".getBytes());

        waitress.takeOrder(requestWithQueryParam.toString(), response.toString());

        assertThat(waitress.serveGetOrder(requestWithQueryParam).toString(), is(response.toString()));
        assertThat(waitress.serveGetOrder(requestWithoutQueryParam).status(), is(Status.NOT_FOUND));
    }

    @Test
    public void serveRequestOrder() {
        waitress.takeOrder(put("/cheese").withHeader(CONTENT_TYPE, TEXT_PLAIN).withInput("cheese".getBytes()).build());

        assertThat(waitress.serveGetOrder(get("/cheese").build()).toString(), is(response(OK).header(CONTENT_TYPE, TEXT_PLAIN).bytes("cheese".getBytes()).toString()));
    }
    
    @Test
    public void importOrders() throws Exception {
        String orders = Strings.toString(getClass().getResourceAsStream("export.txt"));
        
        assertThat(waitress.importOrders(orders).contains("2 orders imported"), is(true));
    }

    @Test
    public void allOrdersWithSpecifiedAuthority() throws Exception {
        waitress.takeOrder("GET http://someserver:1234/some/path HTTP/1.1", "HTTP/1.1 200 OK\n\n<?xml version=\"1.0\" encoding=\"UTF-8\"?><feed xmlns=\"http://www.w3.org/2005/Atom\"><url>http://someserver:1234/foo</url></feed>");
        waitress.takeOrder("GET http://unrelatedServer:1234/some/path HTTP/1.1", "HTTP/1.1 200 OK\n\n<?xml version=\"1.0\" encoding=\"UTF-8\"?><feed xmlns=\"http://www.w3.org/2005/Atom\"><url>http://unrelatedServer:1234/foo</url></feed>");
        String responseWithAuthority = waitress.allOrders(some("anotherServer:4321")).entity().toString();
        assertThat(responseWithAuthority, not(containsString("http://someserver:1234")));
        assertThat(responseWithAuthority, containsString("http://unrelatedServer:1234"));
        String responseWithNoAuthority = waitress.allOrders(Option.<String>none()).entity().toString();
        assertThat(responseWithNoAuthority, containsString("http://someserver:1234/some/path"));
        assertThat(responseWithNoAuthority, containsString("<?xml version=\"1.0\" encoding=\"UTF-8\"?><feed xmlns=\"http://www.w3.org/2005/Atom\"><url>http://someserver:1234/foo</url></feed>"));
    }
}
