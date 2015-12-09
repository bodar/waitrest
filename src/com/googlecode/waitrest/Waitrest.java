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

import static com.googlecode.utterlyidle.ServerConfiguration.SERVER_BASE_PATH;
import static com.googlecode.utterlyidle.ServerConfiguration.SERVER_CLASS;
import static com.googlecode.utterlyidle.ServerConfiguration.SERVER_PORT;

public class Waitrest implements Closeable {
    private Server server;
    private Application application;

    public Waitrest(Properties properties) {
        this.server = ApplicationBuilder.application(Restaurant.class).start(new ServerConfiguration(properties));
        this.application = server.application();
    }

    public Waitrest(String basePath, Integer port) {
        this(serverProperties(basePath, port == null ? null : port.toString(), null));
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

    public static Properties serverProperties(String basePath, String port, String serverClass) {
        Properties properties = new Properties();
        if (basePath != null) properties.setProperty(SERVER_BASE_PATH, basePath);
        if (port != null) properties.setProperty(SERVER_PORT, port);
        if (serverClass != null) properties.setProperty(SERVER_CLASS, serverClass);
        return properties;
    }

}
