package com.googlecode.waitrest;

import com.googlecode.funclate.json.grammar.Grammar;
import com.googlecode.totallylazy.Maps;
import com.googlecode.utterlyidle.Entity;
import com.googlecode.utterlyidle.FormParameters;
import com.googlecode.utterlyidle.QueryParameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.subsetOf;

public class Recipies {

    public static CookBook.EntityRecipe json = new CookBook.EntityRecipe() {
        @Override
        public boolean matches(Entity a, Entity b) {
            return Grammar.VALUE.parse(a.toString()).equals(Grammar.VALUE.parse(b.toString()));
        }
    };

    public static CookBook.EntityRecipe form = new CookBook.EntityRecipe() {
        @Override
        public boolean matches(Entity a, Entity b) {
            return subsetOf(FormParameters.parse(a)).matches(FormParameters.parse(b));
        }
    };

    public static CookBook.QueryParametersRecipe equalQueryParameters = new CookBook.QueryParametersRecipe() {
        @Override
        public boolean matches(QueryParameters a, QueryParameters b) {
            return is(queryAsUnSortedMap(a)).matches(queryAsUnSortedMap(b));
        }
    };

    private static Map<String, List<String>> queryAsUnSortedMap(QueryParameters queryParameters) {
        return Maps.multiMap(new HashMap<String, List<String>>(), queryParameters);
    }


}
