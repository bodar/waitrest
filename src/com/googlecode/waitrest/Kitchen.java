package com.googlecode.waitrest;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.utterlyidle.QueryParameters;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.HttpMethod;

import java.util.HashMap;
import java.util.Map;

import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.Responses.response;
import static com.googlecode.waitrest.Callables.*;

public class Kitchen {
    private Map<Request, Response> orders = new HashMap<Request, Response>();

    public Response receiveOrder(Request request, Response response) {
        return orders.put(request, response);
    }

    public Response receiveOrder(Request request) {
        return orders.put(request, response().
                header(CONTENT_TYPE, request.headers().getValue(CONTENT_TYPE)).
                bytes(request.input()).entity(""));
    }

    public Option<Response> serve(Request request) {
        return sequence(orders.keySet()).
                filter(where(path(), is(request.url().path()))).
                filter(where(query(), contains(request.query()))).
                filter(where(method(), or(is(request.method()), is(HttpMethod.PUT)))).
                headOption().
                map(toOrder());
    }

    public Map<Request, Response> allOrders() {
        return orders;
    }


    private Callable1<Request, Response> toOrder() {
        return new Callable1<Request, Response>() {
            @Override
            public Response call(Request request) throws Exception {
                return orders.get(request);
            }
        };
    }

    private Predicate<? super QueryParameters> contains(final QueryParameters optionalParams) {
        return new Predicate<QueryParameters>() {
            @Override
            public boolean matches(QueryParameters requiredParams) {
                return sequence(requiredParams).forAll(in(sequence(optionalParams)));
            }
        };
    }

}
