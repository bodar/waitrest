package com.googlecode.waitrest;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;

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

    public static Callable1<Response, Status> status() {
        return new Callable1<Response, Status>() {
            @Override
            public Status call(Response response) throws Exception {
                return response.status();
            }
        };
    }
}
