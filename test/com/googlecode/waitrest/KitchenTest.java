package com.googlecode.waitrest;

import com.googlecode.totallylazy.Characters;
import com.googlecode.totallylazy.Option;
import com.googlecode.utterlyidle.Entity;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.MediaType.*;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.RequestBuilder.post;
import static com.googlecode.utterlyidle.ResponseBuilder.response;
import static com.googlecode.utterlyidle.Status.NO_CONTENT;
import static com.googlecode.utterlyidle.Status.OK;
import static com.googlecode.waitrest.Fixtures.getPathToExportFile;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

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
        CookBook cookbook = CookBook.create().recipe(TEXT_PLAIN, new CookBook.EntityRecipe() {
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

    @Test
    public void shouldImportFromFile() throws Exception {
        final String pathToExportFile = getPathToExportFile();

        final int ordersTaken = kitchen.takeOrdersFrom(new File(pathToExportFile));

        assertThat(ordersTaken, is(2));
        assertThat(kitchen.serve(get("/cheese").build()).get().entity().toString(), is("GET gouda\n"));
        assertThat(kitchen.serve(post("/cheese").build()).get().entity().toString(), is("POST gouda\n"));

        Files.delete(Paths.get(pathToExportFile));
    }


    @Test
    public void shouldDeleteBothImMemoryAndImportedFromFileOrders() throws Exception {
        final String pathToExportFile = getPathToExportFile();

        kitchen.takeOrdersFrom(new File(pathToExportFile));

        kitchen.receiveOrder(get("/foo?bar=dan").build(), response().entity("dan").build());

        kitchen.deleteAllOrders();

        assertTrue(kitchen.serve(get("/foo?bar=dan").build()).isEmpty());
        assertTrue(kitchen.serve(get("/cheese").build()).isEmpty());
        assertTrue(kitchen.serve(post("/cheese").build()).isEmpty());

        Files.delete(Paths.get(pathToExportFile));
    }

    @Test
    public void shouldProvideBreakdownOfImportedFromFileOrders() throws Exception {
        final String pathToExportFile = getPathToExportFile();

        kitchen.takeOrdersFrom(new File(pathToExportFile));

        assertThat(kitchen.importedOrderCounts(), is(one(pair(pathToExportFile, 2))));

        Files.delete(Paths.get(pathToExportFile));
    }

}
