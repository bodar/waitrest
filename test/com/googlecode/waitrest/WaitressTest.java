package com.googlecode.waitrest;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.*;
import com.googlecode.utterlyidle.annotations.AnnotatedBindings;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.MediaType.TEXT_PLAIN;
import static com.googlecode.utterlyidle.RequestBuilder.*;
import static com.googlecode.utterlyidle.ResponseBuilder.response;
import static com.googlecode.utterlyidle.Status.OK;
import static com.googlecode.waitrest.Fixtures.getPathToExportFile;
import static com.googlecode.waitrest.Renderers.stringTemplateRenderer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;

public class WaitressTest {
    private Kitchen kitchen = Kitchen.kitchen(CookBook.create());
    private final Resources bindings = new RegisteredResources().add(AnnotatedBindings.annotatedClass(Waitress.class));
    private Redirector redirector = new BaseUriRedirector(new BaseUri(new Uri("")), bindings);
    private Waitress waitress = new Waitress(kitchen, redirector);

    @Test
    public void serveRequestResponseOrder() {
        Request request = get("/cheese").build();
        Response response = response(OK).entity("cheese").build();

        waitress.takeOrder(request.toString(), response.toString());

        assertThat(waitress.serveGetOrder(request).toString(), is(response.toString()));
    }

    @Test
    public void serveRequestResponseOrderWithQueryParams() {
        Request requestWithQueryParam = get("/cheese").query("type", "cheddar").build();
        Request requestWithoutQueryParam = get("/cheese").build();
        Response response = response(OK).entity("cheddar").build();

        waitress.takeOrder(requestWithQueryParam.toString(), response.toString());

        assertThat(waitress.serveGetOrder(requestWithQueryParam).toString(), is(response.toString()));
        assertThat(waitress.serveGetOrder(requestWithoutQueryParam).status(), is(Status.NOT_FOUND));
    }

    @Test
    public void serveRequestOrder() {
        waitress.takeOrder(put("/cheese").header(CONTENT_TYPE, TEXT_PLAIN).entity("cheese").build());

        assertThat(waitress.serveGetOrder(get("/cheese").build()).toString(), is(response(OK).header(CONTENT_TYPE, TEXT_PLAIN).entity("cheese").build().toString()));
    }

    @Test
    public void putOverwrites() {
        waitress.takeOrder(put("/cheese").header(CONTENT_TYPE, TEXT_PLAIN).entity("wensleydale").build());
        waitress.takeOrder(put("/cheese").header(CONTENT_TYPE, TEXT_PLAIN).entity("brie").build());
        assertThat(waitress.serveGetOrder(get("/cheese").build()).toString(), is(response(OK).header(CONTENT_TYPE, TEXT_PLAIN).entity("brie").build().toString()));
    }

    @Test
    public void importOrders() throws Exception {
        String orders = Strings.toString(getClass().getResourceAsStream("export.txt"));
        
        assertThat(waitress.importOrders(orders).contains("2 orders imported"), is(true));
    }

    @Test
    public void preservesLineBreaks() throws Exception {
        String orders = Strings.toString(getClass().getResourceAsStream("linebreaks.txt"));

        waitress.importOrders(orders);
        assertThat(waitress.serveGetOrder(get("/a").build()).entity().toString(), is("Hello\n\nJoe"));
    }

    @Test
    public void allOrders() throws Exception {
        waitress.takeOrder("GET http://someserver:1234/some/path HTTP/1.1", "HTTP/1.1 200 OK\n\n<?xml version=\"1.0\" encoding=\"UTF-8\"?><feed xmlns=\"http://www.w3.org/2005/Atom\"><url>http://someserver:1234/foo</url></feed>");
        waitress.takeOrder("GET http://someserver:1234/some/path HTTP/1.1", "HTTP/1.1 200 OK\n\n<?xml version=\"1.0\" encoding=\"UTF-8\"?><feed xmlns=\"http://www.w3.org/2005/Atom\"><url>http://unrelatedServer:1234/foo</url></feed>");
        String response = stringTemplateRenderer("all").render((Model) waitress.allOrders().entity().value());
        assertThat(response, containsString("GET /some/path"));
        assertThat(response, containsString("<?xml version=\"1.0\" encoding=\"UTF-8\"?><feed xmlns=\"http://www.w3.org/2005/Atom\"><url>http://unrelatedServer:1234/foo</url></feed>"));
    }

    @Test
    public void importsOrdersFromAllOrders() throws Exception {
        waitress.takeOrder("GET http://someserver:1234/some/path HTTP/1.1", "HTTP/1.1 200 OK\n\n<?xml version=\"1.0\" encoding=\"UTF-8\"?><feed xmlns=\"http://www.w3.org/2005/Atom\"><url>http://someserver:1234/foo</url></feed>");
        waitress.takeOrder("GET http://someserver:1234/some/path2 HTTP/1.1", "HTTP/1.1 200 OK\n\n<?xml version=\"1.0\" encoding=\"UTF-8\"?><feed xmlns=\"http://www.w3.org/2005/Atom\"><url>http://unrelatedServer:1234/foo</url></feed>");
        String orders = stringTemplateRenderer("all").render((Model) waitress.allOrders().entity().value());

        assertThat(waitress.importOrders(orders).contains("2 orders imported"), is(true));
    }

    @Test
    public void importsOrdersFromFile() throws Exception {
        final String ordersFile = getPathToExportFile();

        waitress.importOrdersFromFile(ordersFile);

        assertThat(waitress.serveGetOrder(get("/cheese").build()).entity().toString(), is("GET gouda\n"));
        assertThat(waitress.serveGetOrder(post("/cheese").build()).entity().toString(), is("POST gouda\n"));
        Files.delete(Paths.get(ordersFile));
    }

    @Test
    public void deletesOrders() throws Exception {
        final String ordersFile = getPathToExportFile();

        waitress.importOrdersFromFile(ordersFile);

        waitress.deleteAllOrders();

        assertThat(waitress.serveGetOrder(get("/cheese").build()).status(), is(Status.NOT_FOUND));
        assertThat(waitress.serveGetOrder(post("/cheese").build()).status(), is(Status.NOT_FOUND));

        Files.delete(Paths.get(ordersFile));
    }

}
