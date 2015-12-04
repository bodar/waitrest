package com.googlecode.waitrest;

import com.googlecode.utterlyidle.Server;
import com.googlecode.utterlyidle.httpserver.RestServer;

public class Main {
    public static void main(String[] args) throws Exception {
        new Waitrest(port(args), serverClass(args));
    }

    private static Integer port(String[] args) {
        Integer port = null;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception ex) {
                throw new IllegalArgumentException("The port " + args[0] + " should be an integer");
            }
        }
        return port;
    }

    private static Class<? extends Server> serverClass(String[] args) {
        Class<? extends Server> serverClass = RestServer.class;
        if (args.length > 1) {
            try {
                serverClass = (Class<? extends Server>) Class.forName(args[1]);
            } catch (Throwable ex) {
                ex.printStackTrace();
                throw new IllegalArgumentException(String.format( "Invalid server class: %s", args[1]));
            }
        }
        return serverClass;
    }
}
