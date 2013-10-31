package com.googlecode.waitrest;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Characters;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.utterlyidle.Entity;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.MediaType.APPLICATION_FORM_URLENCODED;
import static com.googlecode.utterlyidle.MediaType.APPLICATION_SVG_XML;
import static com.googlecode.utterlyidle.MediaType.TEXT_PLAIN;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.RequestBuilder.post;
import static com.googlecode.utterlyidle.ResponseBuilder.response;
import static com.googlecode.utterlyidle.Status.NO_CONTENT;
import static com.googlecode.utterlyidle.Status.OK;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class KitchenTest {
    private Kitchen kitchen = Kitchen.kitchen(CookBook.create());

    @Test
    public void serveRequestResponseOrder_get() {
        kitchen.receiveOrder(get("/test").build(), response(OK).entity("test entity").build());

        assertThat(kitchen.serve(get("/test").build()).get(), is(response(OK).entity("test entity").build()));
    }

    @Test
    public void serveRequestResponseOrder_post() {
        kitchen.receiveOrder(post("/test").form("formParam", "value").build(), response(OK).entity("test entity").build());

        assertThat(kitchen.serve(post("/test").form("formParam", "value").build()).get(), is(response(OK).entity("test entity").build()));
        assertThat(kitchen.serve(post("/test").build()).isEmpty(), is(true));
    }

    @Test
    public void serveRequestResponseOrder_post_withContentTypePredicate() {
        CookBook cookbook = CookBook.create().recipe(TEXT_PLAIN, new CookBook.Recipe() {
            @Override
            public boolean matches(Entity a, Entity b) {
                return a.toString().equals(b.toString());
            }
        });
        Kitchen kitchen = Kitchen.kitchen(cookbook);
        kitchen.receiveOrder(post("/test").contentType(TEXT_PLAIN).entity("I did this!").build(), response(OK).entity("test entity").build());

        assertThat(kitchen.serve(post("/test").contentType(TEXT_PLAIN).entity("I did this!").build()).get(), is(response(OK).entity("test entity").build()));
        assertThat(kitchen.serve(post("/test").contentType(TEXT_PLAIN).build()).isEmpty(), is(true));
        assertThat(kitchen.serve(post("/test").contentType(APPLICATION_SVG_XML).entity("I did this!").build()).isEmpty(), is(true));
        assertThat(kitchen.serve(post("/test").entity("I did this!").build()).isEmpty(), is(true));
    }

    @Test
    public void serveRequestOrder() {
        String responseContent = "bar";
        kitchen.receiveOrder(get("/foo").build(), response().entity(responseContent).build());
        Response response = kitchen.serve(get("/foo").build()).get();
        assertThat(new String(response.entity().asBytes()), is(responseContent));
    }

    @Test
    public void overridePreviousOrder() {
        kitchen.receiveOrder(get("/test").build(), response(OK).entity("test entity").build());
        kitchen.receiveOrder(get("/test").build(), response(OK).entity("new test entity").build());

        assertThat(kitchen.serve(get("/test").build()).get(), is(response(OK).entity("new test entity").build()));
    }

    @Test
    public void overridePreviousOrderEvenWhenUriIsFullyQualified() {
        kitchen.receiveOrder(get("http://localhost:8899/test").build(), response(OK).entity("test entity").build());
        kitchen.receiveOrder(get("/test").build(), response(OK).entity("new test entity").build());

        assertThat(kitchen.serve(get("http://localhost:8899/test").build()).get(), is(response(OK).entity("new test entity").build()));
    }

    @Test
    public void doNotIgnoreExtraQueryParams() {
        kitchen.receiveOrder(get("/test").build(), response(OK).entity("test entity").build());
        assertThat(kitchen.serve(get("/test?param=doNotIgnore").build()).isEmpty(), is(true));
    }

    @Test
    public void contentTypeIgnoresCharset() {
        kitchen.receiveOrder(post("/test").contentType(APPLICATION_FORM_URLENCODED + "; charset=" + Characters.UTF16).build(), response(OK).entity("test entity").build());
        assertThat(kitchen.serve(post("/test").form("params", "ignore").build()).get(), is(response(OK).entity("test entity").build()));
    }

    @Test
    public void ignoreExtraFormParams() {
        kitchen.receiveOrder(post("/test").contentType(APPLICATION_FORM_URLENCODED).build(), response(OK).entity("test entity").build());
        assertThat(kitchen.serve(post("/test").form("params", "ignore").build()).get(), is(response(OK).entity("test entity").build()));
    }

    @Test
    public void shouldNotServeNonexistentOrder() {
        assertThat(kitchen.serve(get("/test?param=ignore").build()), CoreMatchers.<Option<Response>>is(none(Response.class)));
    }

    @Test
    public void preserveContentTypeWhenServingRequestOrder() {
        String contentType = "text/plain";
        Request get = get("/foo").build();
        kitchen.receiveOrder(get, response().contentType(contentType).entity("bar").build());
        Response response = kitchen.serve(get).get();
        assertThat(response.headers().getValue(CONTENT_TYPE), Matchers.is(contentType));

    }

    @Test
    public void serveOrderWithMatchingQueryParams() {
        kitchen.receiveOrder(get("/foo?bar=dan").build(), response().entity("dan").build());
        Request tom = get("/foo?bar=tom").entity("tom").build();
        kitchen.receiveOrder(tom, response().entity("tom").build());

        Response response = kitchen.serve(tom).get();
        assertThat(response.entity().toString(), Matchers.is("tom"));
    }

    @Test
    public void shouldNotServeOrderWithoutMatchingHttpMethod() {
        kitchen.receiveOrder(post("/foo").build(), response(NO_CONTENT).build());
        assertThat(kitchen.serve(get("/foo").build()).isEmpty(), is(true));
    }
}
