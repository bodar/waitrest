package com.googlecode.waitrest;

import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Strings;
import com.googlecode.utterlyidle.Renderer;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;

import java.util.Map;

import static com.googlecode.totallylazy.Sequences.sequence;

public class Renderers {
    public static Renderer<Map<Request, Response>> orders() {
        return new Renderer<Map<Request, Response>>() {
            public String render(Map<Request, Response> orders) throws Exception {
                return sequence(orders.entrySet()).fold(new StringBuilder(), addOrder()).toString();
            }
        };
    }

    private static Callable2<StringBuilder, Map.Entry<Request, Response>, StringBuilder> addOrder() {
        return new Callable2<StringBuilder, Map.Entry<Request, Response>, StringBuilder>() {
            @Override
            public StringBuilder call(StringBuilder stringBuilder, Map.Entry<Request, Response> requestResponseEntry) throws Exception {
                stringBuilder.append(requestResponseEntry.getKey().toString());
                stringBuilder.append("\n");
                stringBuilder.append(requestResponseEntry.getValue().toString());
                return stringBuilder.append("\n\n---------------------------------------------------\n\n");
            }
        };
    }

    public static Renderer<String> fileContent(final String file) {
        return new Renderer<String>() {
            public String render(String value) throws Exception {
                return Strings.toString(getClass().getResourceAsStream(file));
            }
        };
    }

}
