package com.asquera.elasticsearch.plugins.http;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;

/**
 */
public class HttpAuthenticationPlugin  extends AbstractPlugin {

	 private boolean enabledByDefault = true;
	 private final Settings settings;

	 @Inject public HttpAuthenticationPlugin(Settings settings) {
		 this.settings = settings;
	 }


    @Override public String name() {
        return "http-auth-plugin";
    }

    @Override public String description() {
        return "HTTP Authentication Plugin";
    }

    public void onModule(RestModule restModule) {
      if (settings.getAsBoolean("http.basic.enabled", enabledByDefault)) {
        restModule.addRestAction(HttpBasicAuthRestAction.class);
      }
    }

}
