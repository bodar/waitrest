package com.googlecode.waitrest;

import com.googlecode.utterlyidle.ServerConfiguration;

import java.util.Properties;

import static com.googlecode.utterlyidle.ServerConfiguration.SERVER_CLASS;
import static com.googlecode.utterlyidle.ServerConfiguration.SERVER_PORT;

public class Main {
    public static void main(String[] args) throws Exception {
        new Waitrest(new ServerConfiguration(serverProperties(args)));
    }

    private static Properties serverProperties(String[] args) {
        Properties properties = new Properties();
        if (args.length > 0) properties.setProperty(SERVER_PORT, args[0]);
        if (args.length > 1) properties.setProperty(SERVER_CLASS, args[1]);
        return properties;
    }
}
