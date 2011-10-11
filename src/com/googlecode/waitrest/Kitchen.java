package com.googlecode.waitrest;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Requests;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Responses;
import com.googlecode.utterlyidle.annotations.HttpMethod;
import com.googlecode.utterlyidle.io.HierarchicalPath;

import java.util.HashMap;
import java.util.Map;

import static com.googlecode.totallylazy.Maps.pairs;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.or;
import static com.googlecode.totallylazy.Predicates.subsetOf;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;

public class Kitchen {
    private Map<Request, Response> orders = new HashMap<Request, Response>();

    public Response receiveOrder(Request request, Response response) {
        return orders.put(request, response);
    }

    public Response receiveOrder(Request request) {
        return orders.put(request, Responses.response().
                header(CONTENT_TYPE, request.headers().getValue(CONTENT_TYPE)).
                bytes(request.input()).entity(new String(request.input())));
    }

    public Option<Response> serve(Request request) {
        return sequence(orders.keySet()).
                filter(where(Requests.path(), is(HierarchicalPath.hierarchicalPath(request.uri().path())))).
                filter(where(Requests.form(), subsetOf(Requests.form(request)))).
                filter(where(Requests.query(), is(Requests.query(request)))).
                filter(where(Requests.method(), or(is(request.method()), is(HttpMethod.PUT)))).
                headOption().
                map(response());
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
