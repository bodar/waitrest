package com.googlecode.waitrest;

import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.QueryParameters;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Requests;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.io.HierarchicalPath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.googlecode.totallylazy.Maps.pairs;
import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.utterlyidle.RequestBuilder.modify;
import static com.googlecode.utterlyidle.Requests.query;

public class Kitchen {
    private Map<Request, Response> orders = new ConcurrentHashMap<Request, Response>();

    public Response receiveOrder(Request request, Response response) {
        return orders.put(modify(request).uri(request.uri().dropScheme().dropAuthority()).build(), response);
    }

    public Option<Response> serve(Request request) {
        return sequence(orders.keySet()).
                filter(where(Requests.path(), is(HierarchicalPath.hierarchicalPath(request.uri().path())))).
                filter(where(Requests.form(), subsetOf(Requests.form(request)))).
                filter(where(queryAsUnSortedMap(), is(queryAsUnSortedMap(request)))).
                filter(where(Requests.method(), is(request.method()))).
                headOption().
                map(response());
    }

    private static Callable1<Request, Map<String, List<String>>> queryAsUnSortedMap() {
        return new Callable1<Request, Map<String, List<String>>>() {
            public Map<String, List<String>> call(Request request) throws Exception {
                return queryParametersToUnsortedMap(query(request));
            }
        };
    }

    private static Map<String, List<String>> queryAsUnSortedMap(Request request) {
        return queryParametersToUnsortedMap(QueryParameters.parse(request.uri().query()));
    }

    private static Map<String, List<String>> queryParametersToUnsortedMap(QueryParameters queryParameters) {
        return Maps.multiMap(new HashMap<String, List<String>>(), queryParameters);
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
