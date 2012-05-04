package com.googlecode.waitrest;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.ResponseHandlers;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.utterlyidle.modules.ResponseHandlersModule;
import com.googlecode.yadic.Container;

import static com.googlecode.utterlyidle.PathMatcher.path;
import static com.googlecode.utterlyidle.Requests.method;
import static com.googlecode.utterlyidle.Responses.status;
import static com.googlecode.utterlyidle.Status.BAD_REQUEST;
import static com.googlecode.utterlyidle.Status.CREATED;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;
import static com.googlecode.utterlyidle.annotations.HttpMethod.GET;
import static com.googlecode.utterlyidle.annotations.HttpMethod.POST;
import static com.googlecode.utterlyidle.handlers.RenderingResponseHandler.renderer;
import static com.googlecode.waitrest.Renderers.fileRenderer;
import static com.googlecode.waitrest.Renderers.stringTemplateRenderer;
import static com.googlecode.waitrest.Waitress.WAITRESS_GET_ORDERS_PATH;
import static com.googlecode.waitrest.Waitress.WAITRESS_ORDERS_PATH;
import static com.googlecode.waitrest.Waitress.WAITRESS_ORDER_PATH;

public class Manager implements ResourcesModule, ApplicationScopedModule, ResponseHandlersModule {
    @Override
    public Module addPerApplicationObjects(Container container) {
        container.add(Kitchen.class);
        return this;
    }

    @Override
    public Module addResources(Resources resources) {
        resources.add(annotatedClass(Waitress.class));
        return this;
    }


    @Override
    public Module addResponseHandlers(ResponseHandlers handlers) {
        handlers.add(method(GET).and(path(WAITRESS_ORDER_PATH)), renderer(fileRenderer("menu.html")));
        handlers.add(method(GET).and(path(WAITRESS_GET_ORDERS_PATH)), renderer(stringTemplateRenderer("gets")));
        handlers.add(method(GET).and(path(WAITRESS_ORDERS_PATH)), renderer(stringTemplateRenderer("all")));
        handlers.add(method(POST).and(path(WAITRESS_ORDER_PATH)).and(status(BAD_REQUEST)), renderer(fileRenderer("menu.html")));
        handlers.add(method(POST).and(path(WAITRESS_ORDER_PATH)).and(status(CREATED).and(modelContains("message"))), renderer(fileRenderer("menu.html")));
        handlers.add(method(POST).and(path(WAITRESS_ORDER_PATH)).and(status(CREATED)).and(modelContainsHttpMethod(GET)), renderer(fileRenderer("get.html")));
        handlers.add(method(POST).and(path(WAITRESS_ORDER_PATH)).and(status(CREATED)).and(modelContainsHttpMethod(POST)), renderer(stringTemplateRenderer("post")));
        handlers.add(method(POST).and(path(WAITRESS_ORDER_PATH)).and(status(CREATED)), renderer(fileRenderer("notGetOrPost.html")));
        return this;
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
