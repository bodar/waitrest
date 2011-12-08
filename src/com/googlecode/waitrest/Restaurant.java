package com.googlecode.waitrest;

import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.RestApplication;

public class Restaurant extends RestApplication {
    public Restaurant(BasePath basePath) {
        super(basePath, new Manager());
    }

}
