package com.googlecode.waitrest;

import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.annotations.HttpMethod;
import com.googlecode.utterlyidle.handlers.ResponseHandlers;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.utterlyidle.modules.ResponseHandlersModule;
import com.googlecode.yadic.Container;

import static com.googlecode.totallylazy.Predicates.and;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;
import static com.googlecode.utterlyidle.handlers.RenderingResponseHandler.renderer;
import static com.googlecode.waitrest.Renderers.menuRenderer;
import static com.googlecode.waitrest.Renderers.ordersRenderer;
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
        handlers.add(and(Predicates.method(HttpMethod.GET), Predicates.path(WAITRESS_ORDER_PATH)), renderer(menuRenderer("menu.html")));
        handlers.add(and(Predicates.method(HttpMethod.GET), Predicates.path(WAITRESS_ORDERS_PATH)), renderer(ordersRenderer()));
        handlers.add(and(Predicates.method(HttpMethod.POST), Predicates.path(WAITRESS_ORDER_PATH), Predicates.status(Status.BAD_REQUEST)), renderer(menuRenderer("menu.html")));
        return this;
    }

}
