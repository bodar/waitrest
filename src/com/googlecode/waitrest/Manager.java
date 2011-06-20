package com.googlecode.waitrest;

import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;

public class Manager implements ResourcesModule, ApplicationScopedModule {
    @Override
    public Module addPerApplicationObjects(Container container) {
        container.add(Kitchen.class);
        return this;
    }

    @Override
    public Module addResources(Resources resources) {
        resources.add(Waitress.class);
        return this;
    }
}
