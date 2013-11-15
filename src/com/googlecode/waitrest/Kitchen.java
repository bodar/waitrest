package com.googlecode.waitrest;

import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.regex.Regex;
import com.googlecode.utterlyidle.*;
import com.googlecode.utterlyidle.io.HierarchicalPath;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.googlecode.totallylazy.Maps.pairs;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.RequestBuilder.modify;

public class Kitchen {
    private final Map<Request, Response> orders = new ConcurrentHashMap<Request, Response>();
    private final CookBook cookBook;

    private Kitchen(CookBook cookBook) {
        this.cookBook = cookBook;
    }

    public static Kitchen kitchen(CookBook cookBook) {
        return new Kitchen(cookBook);
    }

    public Response receiveOrder(Request request, Response response) {
        return orders.put(modify(request).uri(request.uri().dropScheme().dropAuthority()).build(), response);
    }

    public Option<Response> serve(Request request) {
        return sequence(orders.keySet()).
                filter(where(Requests.path(), is(HierarchicalPath.hierarchicalPath(request.uri().path())))).
                filter(where(query(), is(cookBook.correctForQueryParameters(Requests.query(request))))).
                filter(where(Requests.method(), is(request.method()))).
                filter(where(header(CONTENT_TYPE), is(stripCharset(request.headers().getValue(CONTENT_TYPE))))).
                filter(where(entity(), is(cookBook.correctForContentType(request)))).
                headOption().
                map(response());
    }

    public String stripCharset(String contentType) {
        return Strings.isEmpty(contentType) ? "" : Regex.regex("^([^\\s^;]+)[;\\s]*").extract(contentType).headOption().getOrElse("");
    }

    private Callable1<Request, String> header(final String header) {
        return new Callable1<Request, String>() {
            @Override
            public String call(Request request) throws Exception {
                return stripCharset(request.headers().getValue(header));
            }
        };
    }

    private Callable1<Request, Entity> entity() {
        return new Callable1<Request, Entity>() {
            @Override
            public Entity call(Request request) throws Exception {
                return request.entity();
            }
        };
    }

    private Callable1<Request, QueryParameters> query() {
        return new Callable1<Request, QueryParameters>() {
            @Override
            public QueryParameters call(Request request) throws Exception {
                return Requests.query(request);
            }
        };
    }

    public Map<Request, Response> allOrders(String orderType) {
        return Maps.map(pairs(orders).filter(orderType(orderType)));
    }

    public Map<Request, Response> allOrders() {
        return orders;
    }

    private Predicate<? super Pair<Request, Response>> orderType(final String orderType) {
        return new Predicate<Pair<Request, Response>>() {
            @Override
            public boolean matches(Pair<Request, Response> requestResponsePair) {
                return requestResponsePair.first().method().equals(orderType);
            }
        };
    }


    private Callable1<Request, Response> response() {
        return new Callable1<Request, Response>() {
            @Override
            public Response call(Request request) throws Exception {
                return orders.get(request);
            }
        };
    }

}
