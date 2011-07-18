package com.googlecode.waitrest;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.utterlyidle.*;
import com.googlecode.utterlyidle.annotations.HttpMethod;

import java.util.HashMap;
import java.util.Map;

import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.waitrest.Predicates.containedBy;

public class Kitchen {
    private Map<Request, Response> orders = new HashMap<Request, Response>();

    public Response receiveOrder(Request request, Response response) {
        return orders.put(request, response);
    }

    public Response receiveOrder(Request request) {
        return orders.put(request, Responses.response().
                header(CONTENT_TYPE, request.headers().getValue(CONTENT_TYPE)).
                bytes(request.input()).entity(""));
    }

    public Option<Response> serve(Request request) {
        return sequence(orders.keySet()).
                filter(where(Requests.path(), is(request.url().path()))).
                filter(where(Requests.form(), containedBy(request.form()))).
                filter(where(Requests.query(), containedBy(request.query()))).
                filter(where(Requests.method(), or(is(request.method()), is(HttpMethod.PUT)))).
                headOption().
                map(response());
    }

    public Map<Request, Response> allOrders() {
        return orders;
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
