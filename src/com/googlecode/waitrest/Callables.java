package com.googlecode.waitrest;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.utterlyidle.Request;

public class Callables {
    public static Callable1<Request, String> method() {
        return new Callable1<Request, String>() {
            @Override
            public String call(Request request) throws Exception {
                return request.method();
            }
        };
    }

    public static Callable1<Request, String> path() {
        return new Callable1<Request, String>() {
            @Override
            public String call(Request request) throws Exception {
                return request.url().path().toString();
            }
        };
    }
}
