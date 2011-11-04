package com.googlecode.waitrest.snooper;

import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.Server;
import com.googlecode.utterlyidle.ServerConfiguration;
import com.googlecode.waitrest.Restaurant;

import java.io.IOException;

import static com.googlecode.utterlyidle.ApplicationBuilder.application;

public class SnoopingWaitrestServer implements Server {

    private Server server = application(Restaurant.class).start(ServerConfiguration.defaultConfiguration().port(1337));

    @Override
    public Uri uri() {
        return server.uri();
    }

    @Override
    public void close() throws IOException {
        server.close();
    }
}
