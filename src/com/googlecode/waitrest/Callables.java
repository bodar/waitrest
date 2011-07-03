package com.googlecode.waitrest;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.utterlyidle.QueryParameters;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.io.HierarchicalPath;

public class Callables {
    public static Callable1<Request, String> method() {
        return new Callable1<Request, String>() {
            @Override
            public String call(Request request) throws Exception {
                return request.method();
            }
        };
    }

    public static Callable1<Request, String> stringPath() {
        return new Callable1<Request, String>() {
            @Override
            public String call(Request request) throws Exception {
                return request.url().path().toString();
            }
        };
    }

    public static Callable1<Request, HierarchicalPath> path() {
        return new Callable1<Request, HierarchicalPath>() {
            @Override
            public HierarchicalPath call(Request request) throws Exception {
                return request.url().path();
            }
        };
    }

    public static Callable1<Request, QueryParameters> query() {
        return new Callable1<Request, QueryParameters>() {
            @Override
            public QueryParameters call(Request request) throws Exception {
                return request.query();
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
