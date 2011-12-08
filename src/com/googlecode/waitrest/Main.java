package com.googlecode.waitrest;

import static com.googlecode.utterlyidle.ApplicationBuilder.application;
import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;

public class Main {
    public static void main(String[] args) throws Exception {
        application(Restaurant.class).start(defaultConfiguration().port(8899));
    }
}
