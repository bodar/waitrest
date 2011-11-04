package com.googlecode.waitrest.snooper;

import com.googlecode.totallylazy.Pair;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.Auditor;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;

import java.util.Date;

import static com.googlecode.utterlyidle.RequestBuilder.post;

public class SnoopingWaitrestHttpAuditor implements Auditor {

    private final SnoopingWaitrestServer snoopingWaitrestServer;

    public SnoopingWaitrestHttpAuditor(SnoopingWaitrestServer snoopingWaitrestServer) {
        this.snoopingWaitrestServer = snoopingWaitrestServer;
    }

    @Override
    public void audit(Pair<Request, Date> requestDatePair, Pair<Response, Date> responseDatePair) throws Exception {
        ClientHttpHandler restClient = new ClientHttpHandler();

        Response response = restClient.handle(post(snoopingWaitrestServer.uri() + "waitrest/order").form("request", requestDatePair.first().toString()).form("response", responseDatePair.first().toString()).build());
        if (response.status().code() >= 400) {
            throw new RuntimeException("Problem snooping on response.\n" + response.toString());
        }

    }
}
