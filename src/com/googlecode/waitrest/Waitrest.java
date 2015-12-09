package com.googlecode.waitrest;

import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.ApplicationBuilder;
import com.googlecode.utterlyidle.Server;
import com.googlecode.utterlyidle.ServerConfiguration;

import java.io.Closeable;
import java.net.URL;
import java.util.Properties;

public class Waitrest implements Closeable {
    private Server server;
    private Application application;

    public Waitrest(Properties properties) {
        ServerConfiguration configuration = new ServerConfiguration(properties);
        try {
            this.server = ApplicationBuilder.application(Restaurant.class).start(configuration);
            this.application = server.application();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't start Waitrest: " + e.getMessage());
        }
    }

    public Waitrest(String basePath, Integer port) {
        this(serverProperties(basePath, port));
    }

    public Waitrest(Integer port) {
        this(null, port);
    }

    public Waitrest() {
        this(new Properties());
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

    private static Properties serverProperties(String basePath, Integer port) {
        Properties properties = new Properties();
        if(basePath != null) properties.setProperty(ServerConfiguration.SERVER_BASE_PATH, basePath);
        if(port != null) properties.setProperty(ServerConfiguration.SERVER_PORT, port.toString());
        return properties;
    }

}
