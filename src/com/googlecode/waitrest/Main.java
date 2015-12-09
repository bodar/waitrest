package com.googlecode.waitrest;

import static com.googlecode.waitrest.Waitrest.serverProperties;

public class Main {
    public static void main(String[] args) throws Exception {
        String serverPort = (args.length > 0)?  args[0]: null;
        String serverClass = (args.length > 1)?  args[1]: null;
        new Waitrest(serverProperties(null, serverPort, serverClass));
    }
}
