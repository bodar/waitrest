package com.googlecode.waitrest;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.utterlyidle.*;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.Sequences.sequence;

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

    public static <T> LogicalPredicate<Iterable<T>> containedBy(final Iterable<T> superset) {
        return new LogicalPredicate<Iterable<T>>() {
            @Override
            public boolean matches(Iterable<T> subset) {
                return sequence(subset).forAll(in(sequence(superset)));
            }
        };
    }
}
