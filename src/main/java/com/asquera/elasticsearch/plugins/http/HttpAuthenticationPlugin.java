package com.asquera.elasticsearch.plugins.http;

import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;

/**
 */
public class HttpAuthenticationPlugin  extends AbstractPlugin {

    @Override public String name() {
        return "http-auth-plugin";
    }

    @Override public String description() {
        return "HTTP Authentication Plugin";
    }

    public void onModule(RestModule restModule) {
        restModule.addRestAction(HttpBasicAuthRestAction.class);
    }

}
