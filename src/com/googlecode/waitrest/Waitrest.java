package com.googlecode.waitrest;

import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.httpserver.RestServer;

import java.io.IOException;
import java.net.URL;

import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;

public class Waitrest {
    private RestServer restServer;

    public Waitrest(String basePath, Integer port) {
        try {
            restServer = new RestServer(new Restaurant(BasePath.basePath(basePath)), port == null ?  defaultConfiguration() : defaultConfiguration().port(port));
        } catch (Exception e) {
            throw new RuntimeException("Couldn't start Waitrest: " + e.getMessage());
        }
    }

    public Waitrest(Integer port) {
        this("/", port);
    }

    public Waitrest() {
        this("/", null);
    }

    public void close() {
        try {
            restServer.close();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't stop Waitrest: " + e.getMessage());
        }
    }

    public URL getURL() {
        return restServer.uri().toURL();
    }

}
