package com.googlecode.waitrest;

import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.*;

import java.io.Closeable;
import java.net.URL;

import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;

public class Waitrest implements Closeable {
    private Server server;
    private Restaurant application;

    public Waitrest(String basePath, ServerConfiguration configuration) {
        try {
            application = new Restaurant(BasePath.basePath(basePath));
            this.server = ApplicationBuilder.application(application).start(configuration);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't start Waitrest: " + e.getMessage());
        }
    }

    public Waitrest(ServerConfiguration serverConfiguration) {
        this("/", serverConfiguration);
    }

    public Waitrest(String basePath, Integer port) {
        this(basePath, serverConfiguration(port));
    }

    public Waitrest(Integer port) {
        this("/", port);
    }

    public Waitrest() {
        this("/", defaultConfiguration());
    }

    public void close() {
        Closeables.safeClose(application);
        Closeables.safeClose(server);
    }

    public URL getURL() {
        return uri().toURL();
    }

    public Uri uri() {
        return server.uri();
    }

    public Server server() {
        return server;
    }

    public Application application() {
        return application;
    }

    private static ServerConfiguration serverConfiguration(Integer port) {
        return (port == null ? defaultConfiguration() : defaultConfiguration().port(port));
    }

}
