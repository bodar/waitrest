package com.googlecode.waitrest;

public class Main {
    public static void main(String[] args) throws Exception {
        new Waitrest(port(args));
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
}
