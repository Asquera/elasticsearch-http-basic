package com.asquera.elasticsearch.plugins.http;

import org.elasticsearch.http.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.service.NodeService;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.Base64;

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
    
    public void internalDispatchRequest(final HttpRequest request, final HttpChannel channel) {
        if (authBasic(request)) {
            super.internalDispatchRequest(request, channel);
        } else {
            channel.sendResponse(new StringRestResponse(FORBIDDEN));
        }
    }
    
    private boolean authBasic(final HttpRequest request){
        String authHeader = request.header("Authorization");
        
        if (authHeader == null) {
            return false;
        }
        
        String[] split = authHeader.split(" ");
        String decoded = null;
        
        try {
            decoded = new String(Base64.decode(split[1]));
        } catch (IOException e) {
            logger.warn("Decoding of basic auth failed.");
            return false;
        }
        
        String[] user_and_password = decoded.split(":");
        String given_user = user_and_password[0];
        String given_pass = user_and_password[1];
        
        if (this.user.equals(given_user) &&
            this.password.equals(given_pass)) {
            return true;
        } else {
            return false;
        }
    }
}