package com.googlecode.waitrest.snooper;

import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.AuditHandler;
import com.googlecode.utterlyidle.handlers.HttpClient;

public class SnoopingHttpClient implements HttpClient {
    
    private HttpClient httpClient;

    public SnoopingHttpClient(HttpClient httpClient, SnoopingWaitrestServer server) {
        this.httpClient = new AuditHandler(httpClient, new SnoopingWaitrestHttpAuditor(server));
    }

    @Override
    public Response handle(Request request) throws Exception {
        return httpClient.handle(request);
    }
}
