package com.googlecode.waitrest;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Requests;
import com.googlecode.utterlyidle.Response;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;

public class Predicates {
    public static LogicalPredicate<Pair<Request, Response>> path(final String path) {
        return where(first(Request.class), where(asString(Requests.path()), is(path)));
    }

    private static <I, O> Callable1<I, String> asString(final Callable1<I, O> callable) {
        return new Callable1<I, String>() {
            @Override
            public String call(I input) throws Exception {
                return callable.call(input).toString();
            }
        };
    }
}
