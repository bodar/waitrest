package com.googlecode.waitrest;

import com.googlecode.totallylazy.First;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.utterlyidle.Request;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;

public class Predicates {
    public static LogicalPredicate<First<Request>> path(final String path) {
        return where(first(Request.class), where(Callables.path(), is(path)));
    }

    public static LogicalPredicate<First<Request>> method(final String method) {
        return where(first(Request.class), where(Callables.method(), is(method)));
    }
}
