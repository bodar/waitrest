package com.googlecode.waitrest;

import com.googlecode.funclate.Model;
import com.googlecode.funclate.stringtemplate.EnhancedStringTemplateGroup;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Strings;
import com.googlecode.utterlyidle.Renderer;
import com.googlecode.utterlyidle.rendering.ObjectRenderer;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.net.URL;
import java.util.Map;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.URLs.packageUrl;
import static java.lang.String.format;

public class Renderers {

    public static Renderer<Model> fileRenderer(final String file) {
        return new Renderer<Model>() {
            public String render(Model model) throws Exception {
                String fileContent = Strings.toString(getClass().getResourceAsStream(file));
                return removeUnusedPlaceholders(sequence(model.entries()).fold(fileContent, replacePlaceholder()));
            }
        };
    }

    public static Renderer<Model> stringTemplateRenderer(final String file) {
        return new Renderer<Model>() {
            @Override
            public String render(Model model) throws Exception {
                URL url = packageUrl(Renderers.class);
                StringTemplateGroup group = new EnhancedStringTemplateGroup(url);
                StringTemplate template = group.getInstanceOf(file, model.toMap());
                return template.toString();
            }
        };
    }

    public static Renderer<Object> objectRenderer() {
        return new ObjectRenderer();
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
