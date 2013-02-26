package com.asquera.elasticsearch.plugins.http;

import org.elasticsearch.http.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.service.NodeService;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.Base64;

import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.StringRestResponse;

import static org.elasticsearch.rest.RestStatus.*;

import java.io.IOException;

/**
 * @author Florian Gilcher (florian.gilcher@asquera.de)
 */
public class HttpBasicServer extends HttpServer {

    private final String user;
    private final String password;

    @Inject public HttpBasicServer(Settings settings, Environment environment, HttpServerTransport transport,
            RestController restController,
            NodeService nodeService) {
        super(settings, environment, transport, restController, nodeService);

        this.user = settings.get("http.basic.user");
        this.password = settings.get("http.basic.password");
    }

    @Override
    public void internalDispatchRequest(final HttpRequest request, final HttpChannel channel) {
        if (shouldLetPass(request) || authBasic(request)) {
            super.internalDispatchRequest(request, channel);
        } else {
            channel.sendResponse(new StringRestResponse(UNAUTHORIZED));
        }
    }

    private boolean shouldLetPass(final HttpRequest request) {
        return (request.method() == RestRequest.Method.GET) && request.path().equals("/");
    }

    private boolean authBasic(final HttpRequest request) {
        String authHeader = request.header("Authorization");
        if (authHeader == null)
            return false;

        String[] split = authHeader.split(" ");
        if (split.length < 1 || !split[0].equals("Basic"))
            return false;

        String decoded;
        try {
            decoded = new String(Base64.decode(split[1]));
        } catch (IOException e) {
            logger.warn("Decoding of basic auth failed.");
            return false;
        }

        String[] userAndPassword = decoded.split(":");
        String givenUser = userAndPassword[0];
        String givenPass = userAndPassword[1];
        return this.user.equals(givenUser) && this.password.equals(givenPass);
    }
}