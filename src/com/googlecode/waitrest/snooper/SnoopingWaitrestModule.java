package com.googlecode.waitrest.snooper;

import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.yadic.Container;

public class SnoopingWaitrestModule implements RequestScopedModule, ApplicationScopedModule {

    @Override
    public Module addPerRequestObjects(Container container) throws Exception {
        container.decorate(HttpClient.class, SnoopingHttpClient.class);
        return this;
    }

    @Override
    public Module addPerApplicationObjects(Container container) throws Exception {
        container.add(SnoopingWaitrestServer.class);
        return this;
    }
}
