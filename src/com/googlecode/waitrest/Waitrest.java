package com.googlecode.waitrest;

import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.Server;
import com.googlecode.utterlyidle.ServerConfiguration;
import com.googlecode.utterlyidle.httpserver.RestServer;

import java.io.Closeable;
import java.net.URL;

import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;

public class Waitrest implements Closeable {
    private Server server;
    private Restaurant application;

    public Waitrest(String basePath, Integer port, Class<? extends Server> serverClass) {
        try {
            application = new Restaurant(BasePath.basePath(basePath));
            this.server = initServer(port, serverClass);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't start Waitrest: " + e.getMessage());
        }
    }

    public Waitrest(Integer port, Class<? extends Server> serverClass) {
        this("/", port, serverClass);
    }

    public Waitrest(String basePath, Integer port) {
        this(basePath, port, RestServer.class);
    }

    public Waitrest(Integer port) {
        this("/", port);
    }

    public Waitrest() {
        this("/", null);
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

    private Server initServer(Integer port, Class<? extends Server> serverClass) throws Exception {
        ServerConfiguration configuration = (port == null ? defaultConfiguration() : defaultConfiguration().port(port));
        if(serverClass.equals(RestServer.class)){
            return new RestServer(application, configuration);
        } else if(serverClass.equals(com.googlecode.utterlyidle.jetty.RestServer.class)){
            return com.googlecode.utterlyidle.jetty.RestServer.restServer(application, configuration);
        }
        throw new RuntimeException(String.format("The server class %s is currently not supported", serverClass.getName()));
    }

}
