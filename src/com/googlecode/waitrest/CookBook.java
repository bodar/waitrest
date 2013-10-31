package com.googlecode.waitrest;

import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.collections.HashTreeMap;
import com.googlecode.totallylazy.collections.PersistentMap;
import com.googlecode.totallylazy.regex.Regex;
import com.googlecode.utterlyidle.Entity;
import com.googlecode.utterlyidle.FormParameters;
import com.googlecode.utterlyidle.Request;

import static com.googlecode.totallylazy.Predicates.subsetOf;
import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.MediaType.APPLICATION_FORM_URLENCODED;
import static com.googlecode.utterlyidle.MediaType.APPLICATION_JSON;

public class CookBook {
    private final PersistentMap<String, Recipe> recipies;

    private CookBook(PersistentMap<String, Recipe> recipies) {
        this.recipies = recipies;
    }

    public Predicate<Entity> correctForContentType(final Request request) {
        return request.headers().valueOption(CONTENT_TYPE).flatMap(new Callable1<String, Option<Predicate<Entity>>>() {
            @Override
            public Option<Predicate<Entity>> call(String contentType) throws Exception {
                return recipies.lookup(stripCharset(contentType)).map(new Callable1<Recipe, Predicate<Entity>>() {
                    @Override
                    public Predicate<Entity> call(final Recipe recipe) throws Exception {
                        return new Predicate<Entity>() {
                            @Override
                            public boolean matches(Entity other) {
                                return recipe.matches(request.entity(), other);
                            }
                        };
                    }
                });
            }
        }).getOrElse(new Predicate<Entity>() {
            @Override
            public boolean matches(Entity other) {
                return java.util.Arrays.equals(other.asBytes(), request.entity().asBytes());
            }
        });
    }

    private String stripCharset(String contentType) {
        return Strings.isEmpty(contentType) ? "" : Regex.regex("^([^\\s^;]+)[;\\s]*").extract(contentType).headOption().getOrElse("");
    }

    public static CookBook create() {
        PersistentMap<String, Recipe> recipes = HashTreeMap.<String, Recipe>hashTreeMap().
                insert(APPLICATION_FORM_URLENCODED, Recipies.form).
                insert(APPLICATION_JSON, Recipies.json);
        return new CookBook(recipes);
    }

    public CookBook recipe(String contentType, Recipe recipe){
        return new CookBook(recipies.insert(contentType, recipe));
    }

    interface Recipe extends BinaryPredicate<Entity> {

    }
}
