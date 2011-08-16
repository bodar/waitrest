package com.googlecode.waitrest;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Strings;
import com.googlecode.utterlyidle.Renderer;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;

import java.util.Map;

import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.String.format;

public class Renderers {
    public static Renderer<Map<Request, Response>> textRenderer() {
        return new Renderer<Map<Request, Response>>() {
            public String render(Map<Request, Response> orders) throws Exception {
                return sequence(orders.entrySet()).map(toDisplay()).toString("\n\n---------------------------------------------------\n\n");
            }
        };
    }

    private static Callable1<Map.Entry<Request, Response>, String> toDisplay() {
        return new Callable1<Map.Entry<Request, Response>, String>() {
            @Override
            public String call(Map.Entry<Request, Response> requestResponseEntry) throws Exception {
                return format("%s\n\n%s", requestResponseEntry.getKey(), requestResponseEntry.getValue());
            }
        };
    }

    public static Renderer<Model> fileRenderer(final String file) {
        return new Renderer<Model>() {
            public String render(Model model) throws Exception {
                String fileContent = Strings.toString(getClass().getResourceAsStream(file));
                return removeUnusedPlaceholders(sequence(model.entries()).fold(fileContent, replacePlaceholder()));
            }
        };
    }

    private static String removeUnusedPlaceholders(String content) {
        return content.replaceAll(format("\\$\\{.*\\}"), "");
    }

    private static Callable2<String, Map.Entry<String, Object>, String> replacePlaceholder() {
        return new Callable2<String, Map.Entry<String, Object>, String>() {
            @Override
            public String call(String response, Map.Entry<String, Object> modelEntry) throws Exception {
                return response.replaceAll(format("\\$\\{%s\\}", modelEntry.getKey()), (String) modelEntry.getValue());
            }
        };
    }



}
