package com.googlecode.waitrest;

import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.collections.HashTreeMap;
import com.googlecode.totallylazy.collections.PersistentMap;
import com.googlecode.totallylazy.regex.Regex;
import com.googlecode.utterlyidle.Entity;
import com.googlecode.utterlyidle.QueryParameters;
import com.googlecode.utterlyidle.Request;

import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.MediaType.APPLICATION_FORM_URLENCODED;
import static com.googlecode.utterlyidle.MediaType.APPLICATION_JSON;

public class CookBook {
    private final PersistentMap<String, EntityRecipe> entityRecipies;
    private final QueryParametersRecipe queryParametersRecipe;

    private CookBook(PersistentMap<String, EntityRecipe> entityRecipies, QueryParametersRecipe queryParametersRecipe) {
        this.entityRecipies = entityRecipies;
        this.queryParametersRecipe = queryParametersRecipe;
    }

    public Predicate<QueryParameters> correctForQueryParameters(final QueryParameters toMatch) {
        return new Predicate<QueryParameters>() {
            @Override
            public boolean matches(QueryParameters other) {
                return queryParametersRecipe.matches(toMatch, other);
            }
        };
    }

    public Predicate<Entity> correctForContentType(final Request request) {
        return request.headers().valueOption(CONTENT_TYPE).flatMap(new Callable1<String, Option<Predicate<Entity>>>() {
            @Override
            public Option<Predicate<Entity>> call(String contentType) throws Exception {
                return entityRecipies.lookup(stripCharset(contentType)).map(new Callable1<EntityRecipe, Predicate<Entity>>() {
                    @Override
                    public Predicate<Entity> call(final EntityRecipe entityRecipe) throws Exception {
                        return new Predicate<Entity>() {
                            @Override
                            public boolean matches(Entity other) {
                                return entityRecipe.matches(request.entity(), other);
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
        PersistentMap<String, EntityRecipe> defaultEntityRecipes = HashTreeMap.<String, EntityRecipe>hashTreeMap().
                insert(APPLICATION_FORM_URLENCODED, Recipies.form).
                insert(APPLICATION_JSON, Recipies.json);
        QueryParametersRecipe defaultQueryParametersRecipe =
                Recipies.equalQueryParameters;
        return new CookBook(defaultEntityRecipes, defaultQueryParametersRecipe);
    }

    public CookBook recipe(String contentType, EntityRecipe entityRecipe) {
        return new CookBook(entityRecipies.insert(contentType, entityRecipe), queryParametersRecipe);
    }

    public CookBook queryParametersRecipe(QueryParametersRecipe recipe){
        return new CookBook(entityRecipies, recipe);
    }

    public interface EntityRecipe extends BinaryPredicate<Entity> {
    }

    public interface QueryParametersRecipe extends BinaryPredicate<QueryParameters> {
    }
}
