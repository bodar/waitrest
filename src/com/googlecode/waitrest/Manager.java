package com.googlecode.waitrest;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.handlers.ResponseHandlers;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.utterlyidle.modules.ResponseHandlersModule;
import com.googlecode.yadic.Container;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Predicates.matches;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.utterlyidle.MediaType.APPLICATION_JSON;
import static com.googlecode.utterlyidle.MediaType.TEXT_HTML;
import static com.googlecode.utterlyidle.PathMatcher.path;
import static com.googlecode.utterlyidle.Requests.accept;
import static com.googlecode.utterlyidle.Requests.method;
import static com.googlecode.utterlyidle.Responses.status;
import static com.googlecode.utterlyidle.Status.BAD_REQUEST;
import static com.googlecode.utterlyidle.Status.CREATED;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;
import static com.googlecode.utterlyidle.annotations.HttpMethod.GET;
import static com.googlecode.utterlyidle.annotations.HttpMethod.POST;
import static com.googlecode.utterlyidle.handlers.RenderingResponseHandler.renderer;
import static com.googlecode.waitrest.Renderers.fileRenderer;
import static com.googlecode.waitrest.Renderers.objectRenderer;
import static com.googlecode.waitrest.Renderers.stringTemplateRenderer;
import static com.googlecode.waitrest.Waitress.*;

public class Manager implements ResourcesModule, ApplicationScopedModule, ResponseHandlersModule {
    @Override
    public Container addPerApplicationObjects(Container container) {
        return container.add(Kitchen.class)
                .addInstance(CookBook.class, CookBook.create());
    }

    @Override
    public Resources addResources(Resources resources) {
        return resources.add(annotatedClass(Waitress.class));
    }

    @Override
    public ResponseHandlers addResponseHandlers(ResponseHandlers handlers) {
        handlers.add(method(GET).and(status(Status.OK).and(path(WAITRESS_ORDER_PATH))), renderer(fileRenderer("menu.html")));
        handlers.add(method(GET).and(status(Status.OK)).and(path(WAITRESS_GET_ORDERS_PATH)), renderer(stringTemplateRenderer("gets")));
        handlers.add(method(GET).and(status(Status.OK)).and(path(WAITRESS_ORDER_COUNTS_PATH)).and(where(first(Request.class), where(accept(), matches(TEXT_HTML)))), renderer(stringTemplateRenderer("counts")));
        handlers.add(method(GET).and(status(Status.OK)).and(path(WAITRESS_ORDER_COUNTS_PATH)).and(where(first(Request.class), where(accept(), matches(APPLICATION_JSON)))), renderer(objectRenderer()));
        handlers.add(method(GET).and(status(Status.OK)).and(path(WAITRESS_ORDERS_PATH)), renderer(stringTemplateRenderer("all")));
        handlers.add(method(POST).and(status(Status.OK)).and(path(WAITRESS_ORDER_PATH)).and(status(BAD_REQUEST)), renderer(fileRenderer("menu.html")));
        handlers.add(method(POST).and(status(Status.OK)).and(path(WAITRESS_ORDER_PATH)).and(status(CREATED).and(modelContains("message"))), renderer(fileRenderer("menu.html")));
        handlers.add(method(POST).and(status(Status.OK)).and(path(WAITRESS_ORDER_PATH)).and(status(CREATED)).and(modelContainsHttpMethod(GET)), renderer(fileRenderer("get.html")));
        handlers.add(method(POST).and(status(Status.OK)).and(path(WAITRESS_ORDER_PATH)).and(status(CREATED)).and(modelContainsHttpMethod(POST)), renderer(stringTemplateRenderer("post")));
        handlers.add(method(POST).and(status(Status.OK)).and(path(WAITRESS_ORDER_PATH)).and(status(CREATED)), renderer(fileRenderer("notGetOrPost.html")));
        return handlers;
    }

    private Predicate<? super Pair<Request, Response>> modelContains(final String key) {
        return new Predicate<Pair<Request, Response>>() {
            @Override
            public boolean matches(Pair<Request, Response> requestResponsePair) {
                return ((Model) requestResponsePair.second().entity().value()).contains(key);
            }
        };
    }

    private Predicate<Pair<Request, Response>> modelContainsHttpMethod(final String httpMethod) {
        return new Predicate<Pair<Request, Response>>() {
            @Override
            public boolean matches(Pair<Request, Response> requestResponsePair) {
                return ((Model) requestResponsePair.second().entity().value()).get("method", String.class).equalsIgnoreCase(httpMethod);
            }
        };
    }
}