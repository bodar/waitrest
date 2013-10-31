package com.googlecode.waitrest;

import com.googlecode.funclate.json.grammar.Grammar;
import com.googlecode.utterlyidle.Entity;
import com.googlecode.utterlyidle.FormParameters;

import static com.googlecode.totallylazy.Predicates.subsetOf;

public class Recipies {

    public static CookBook.Recipe json = new CookBook.Recipe() {
        @Override
        public boolean matches(Entity a, Entity b) {
            return Grammar.VALUE.parse(a.toString()).equals(Grammar.VALUE.parse(b.toString()));
        }
    };

    public static CookBook.Recipe form = new CookBook.Recipe() {
        @Override
        public boolean matches(Entity a, Entity b) {
            return subsetOf(FormParameters.parse(a)).matches(FormParameters.parse(b));
        }
    };
}
