package com.googlecode.waitrest;

import com.googlecode.utterlyidle.ServerConfiguration;
import com.googlecode.utterlyidle.httpserver.RestServer;

public class Main {
    public static void main(String[] args) throws Exception {
        new RestServer(new Restaurant(), ServerConfiguration.defaultConfiguration().port(8899));
    }
}
