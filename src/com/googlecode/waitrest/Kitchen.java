package com.googlecode.waitrest;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.utterlyidle.QueryParameters;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.io.HierarchicalPath;

import java.util.HashMap;
import java.util.Map;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.Responses.response;

public class Kitchen {
    private Map<Request, Response> orders = new HashMap<Request, Response>();

    public Response receiveOrder(Request request, Response response) {
        return orders.put(request, response);
    }

    public Response receiveOrder(Request request) {
        return orders.put(request, response().
                header(CONTENT_TYPE, request.headers().getValue(CONTENT_TYPE)).
                entity(request.input()));
    }

    public Option<Response> serve(Request request) {
        Option<Request> recordedRequest = sequence(orders.keySet()).filter(where(path(), is(request.url().path()))).filter(where(query(), contains(request.query()))).headOption();
        if(!recordedRequest.isEmpty()) {
            Response recordedResponse = orders.get(recordedRequest.get());
            return some(recordedResponse);
        }
        return none(Response.class);
    }

    public Integer countAll() {
        return orders.size();
    }

    private Callable1<? super Request, HierarchicalPath> path() {
        return new Callable1<Request, HierarchicalPath>() {
            @Override
            public HierarchicalPath call(Request request) throws Exception {
                return request.url().path();
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

    private Callable1<? super Request, QueryParameters> query() {
        return new Callable1<Request, QueryParameters>() {
            @Override
            public QueryParameters call(Request request) throws Exception {
                return request.query();
            }
        };
    }
}
