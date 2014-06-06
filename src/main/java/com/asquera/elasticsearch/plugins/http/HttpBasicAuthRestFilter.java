package com.asquera.elasticsearch.plugins.http;

import static org.elasticsearch.rest.RestStatus.UNAUTHORIZED;

import java.io.IOException;

import org.elasticsearch.common.Base64;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;

public class HttpBasicAuthRestFilter extends AbstractAuthRestFilter {

	protected HttpBasicAuthRestFilter(Settings settings) {
		super(settings);
	}

	protected void sendAuthenticationChallenge(RestRequest request, RestChannel channel) {
        String addr = getAddress(request);
        logger.error("UNAUTHORIZED type:{}, address:{}, path:{}, request:{}, content:{}, credentials:{}",
                request.method(), addr, request.path(), request.params(), request.content().toUtf8(), getDecoded(request));

        BytesRestResponse response = new BytesRestResponse(UNAUTHORIZED, "Authentication Required");
        response.addHeader("WWW-Authenticate", "Basic realm=\"Restricted\"");
        channel.sendResponse(response);
	}
	
    private String getDecoded(RestRequest request) {
        String authHeader = request.header("Authorization");
        if (authHeader == null)
            return "";

        String[] split = authHeader.split(" ", 2);
        if (split.length != 2 || !split[0].equals("Basic"))
            return "";
        try {
            return new String(Base64.decode(split[1]));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

	@Override
	protected boolean isAuthenticated(RestRequest request) {
        String decoded = "";
        try {
            decoded = getDecoded(request);
            if (!decoded.isEmpty()) {
                String[] userAndPassword = decoded.split(":", 2);
                String givenUser = userAndPassword[0];
                String givenPass = userAndPassword[1];
                if (this.user.equals(givenUser) && this.password.equals(givenPass))
                    return true;
            }
        } catch (Exception e) {
            logger.warn("Retrieving of user and password failed for " + decoded + " ," + e.getMessage());
        }
        return false;
	}

	
}
