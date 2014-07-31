package com.asquera.elasticsearch.plugins.http;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;

public class HttpBasicAuthRestAction extends BaseRestHandler {

    @Inject
    public HttpBasicAuthRestAction(Settings settings, Client client, RestController controller) {
        super(settings, client);
        boolean isHawk = settings.get("http.authentication.scheme", "basic").equals("hawk");
        AbstractAuthRestFilter authFilter = isHawk ? new HttpHawkAuthRestFilter(settings)
        		: new HttpBasicAuthRestFilter(settings);
        controller.registerFilter(authFilter);
    }

	@Override
	protected void handleRequest(RestRequest request, RestChannel channel,
			Client client) throws Exception {
		// no-op: never called as it is never registered
	}

}
