package com.googlecode.waitrest;

import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Strings;
import com.googlecode.utterlyidle.Renderer;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;

import java.util.Map;

import static com.googlecode.totallylazy.Sequences.sequence;

public class Renderers {
    public static Renderer<Map<Request, Response>> ordersRenderer() {
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

    public static Renderer<Map<String, String>> menuRenderer(final String file) {
        return new Renderer<Map<String, String>>() {
            public String render(Map<String, String> model) throws Exception {
                String fileContent = Strings.toString(getClass().getResourceAsStream(file));
                return removeUnusedPlaceholders(sequence(model.entrySet()).fold(fileContent, replacePlaceholder()));
            }
        };
    }

    private static String removeUnusedPlaceholders(String content) {
        return content.replaceAll(String.format("\\$\\{.*\\}"), "");
    }

    private static Callable2<String, Map.Entry<String, String>, String> replacePlaceholder() {
        return new Callable2<String, Map.Entry<String, String>, String>() {
            @Override
            public String call(String response, Map.Entry<String, String> modelEntry) throws Exception {
                return response.replaceAll(String.format("\\$\\{%s\\}", modelEntry.getKey()), modelEntry.getValue());
            }
        };
    }



}
