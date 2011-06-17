package com.googlecode.waitrest;

import com.googlecode.utterlyidle.RestApplication;

public class Restaurant extends RestApplication {
    public Restaurant() {
        super(new Manager());
    }

}
