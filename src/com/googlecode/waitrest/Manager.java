package com.googlecode.waitrest;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.First;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.utterlyidle.*;
import com.googlecode.utterlyidle.annotations.HttpMethod;
import com.googlecode.utterlyidle.handlers.ResponseHandlers;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.utterlyidle.modules.ResponseHandlersModule;
import com.googlecode.yadic.Container;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.utterlyidle.handlers.RenderingResponseHandler.renderer;
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
        handlers.add(Predicates.and(method(HttpMethod.GET), path(WAITRESS_ORDER_PATH)), renderer(fileContent("menu.html")));
        return this;
    }

    private Renderer<String> fileContent(final String file) {
        return new Renderer<String>() {
            public String render(String value) throws Exception {
                return Strings.toString(getClass().getResourceAsStream(file));
            }
        };
    }

    private LogicalPredicate<First<Request>> path(final String path) {
        return where(first(Request.class), where(path(), is(path)));
    }

    private LogicalPredicate<First<Request>> method(final String method) {
        return where(first(Request.class), where(method(), is(method)));
    }

    private Callable1<Request, String> method() {
        return new Callable1<Request, String>() {
            @Override
            public String call(Request request) throws Exception {
                return request.method();
            }
        };
    }

    private Callable1<Request, String> path() {
        return new Callable1<Request, String>() {
            @Override
            public String call(Request request) throws Exception {
                return request.url().path().toString();
            }
        };
    }
}
