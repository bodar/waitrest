package com.googlecode.waitrest;

import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.httpserver.RestServer;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;

public class Waitrest implements Closeable {
    private RestServer restServer;
    private Restaurant application;

    public Waitrest(String basePath, Integer port) {
        try {
            application = new Restaurant(BasePath.basePath(basePath));
            restServer = new RestServer(application, port == null ?  defaultConfiguration() : defaultConfiguration().port(port));
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
        Closeables.safeClose(application);
        Closeables.safeClose(restServer);
    }

    public URL getURL() {
        return uri().toURL();
    }

    public Uri uri() {
        return restServer.uri();
    }

}
