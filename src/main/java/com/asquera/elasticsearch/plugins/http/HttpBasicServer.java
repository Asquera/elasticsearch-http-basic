package com.asquera.elasticsearch.plugins.http;

import org.elasticsearch.http.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.service.NodeService;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.Base64;

import org.elasticsearch.rest.RestRequest;

import static org.elasticsearch.rest.RestStatus.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.rest.RestRequest.Method;
import org.elasticsearch.rest.StringRestResponse;

// # possible http config
// http.basic.user: admin
// http.basic.password: password
// http.basic.ipwhitelist: ["localhost", "somemoreip"]
// http.basic.xforward: "X-Forwarded-For"
// # if you use javascript
// # EITHER $.ajaxSetup({ headers: { 'Authorization': "Basic " + credentials }});
// # OR use beforeSend in  $.ajax({
// http.cors.allow-headers: "X-Requested-With, Content-Type, Content-Length, Authorization"
// 
/**
 * @author Florian Gilcher (florian.gilcher@asquera.de)
 * @author Peter Karich
 */
public class HttpBasicServer extends HttpServer {

    private final String user;
    private final String password;
    private final Set<String> whitelist;
    private final String xForwardFor;
    private final boolean log;

    @Inject public HttpBasicServer(Settings settings, Environment environment, HttpServerTransport transport,
            RestController restController,
            NodeService nodeService) {
        super(settings, environment, transport, restController, nodeService);

        this.user = settings.get("http.basic.user", "admin");
        this.password = settings.get("http.basic.password", "admin_pw");
        this.whitelist = new HashSet<String>(Arrays.asList(
                settings.getAsArray("http.basic.ipwhitelist",
                new String[]{"localhost", "127.0.0.1"})));

        // for AWS load balancers it is X-Forwarded-For -> hmmh does not work 
        this.xForwardFor = settings.get("http.basic.xforward", "");
        this.log = settings.getAsBoolean("http.basic.log", false);
        Loggers.getLogger(getClass()).info("using {}:{} with whitelist {}, xforward {}",
                user, password, whitelist, xForwardFor);
    }

    @Override
    public void internalDispatchRequest(final HttpRequest request, final HttpChannel channel) {
        if (log)
            logger.info("Authorization:{}, host:{}, xforward:{}, path:{}, isInWhitelist:{}, Client-IP:{}, X-Client-IP:{}",
                    request.header("Authorization"), request.header("host"),
                    request.header(xForwardFor), request.path(), isInIPWhitelist(request),
                    request.header("X-Client-IP"), request.header("Client-IP"));

        // allow health check even without authorization
        if (healthCheck(request)) {
            channel.sendResponse(new StringRestResponse(OK, "{\"OK\":{}}"));
        } else if (allowOptionsForCORS(request) || authBasic(request) || isInIPWhitelist(request)) {
            super.internalDispatchRequest(request, channel);
        } else {
            String addr = getAddress(request);
            Loggers.getLogger(getClass()).error("UNAUTHORIZED type:{}, address:{}, path:{}, request:{}, content:{}, credentials:{}",
                    request.method(), addr, request.path(), request.params(), request.content().toUtf8(), getDecoded(request));

            StringRestResponse response = new StringRestResponse(UNAUTHORIZED, "Authentication Required");
            response.addHeader("WWW-Authenticate", "Basic realm=\"Restricted\"");
            channel.sendResponse(response);
        }
    }

    private boolean healthCheck(final HttpRequest request) {
        String path = request.path();
        return (request.method() == RestRequest.Method.GET) && path.equals("/");
    }

    public String getDecoded(HttpRequest request) {
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

    private boolean authBasic(final HttpRequest request) {
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

    private String getAddress(HttpRequest request) {
        String addr;
        if (xForwardFor.isEmpty()) {
            addr = request.header("Host");
            addr = addr == null ? "" : addr;
        } else {
            addr = request.header(xForwardFor);
            addr = addr == null ? "" : addr;
            int addrIndex = addr.indexOf(',');
            if (addrIndex >= 0)
                addr = addr.substring(0, addrIndex);
        }

        int portIndex = addr.indexOf(":");
        if (portIndex >= 0)
            addr = addr.substring(0, portIndex);
        return addr;
    }

    private boolean isInIPWhitelist(HttpRequest request) {
        String addr = getAddress(request);
//        Loggers.getLogger(getClass()).info("address {}, path {}, request {}",
//                addr, request.path(), request.params());
        if (whitelist.isEmpty() || addr.isEmpty())
            return false;
        return whitelist.contains(addr);
    }

    /**
     * https://en.wikipedia.org/wiki/Cross-origin_resource_sharing the
     * specification mandates that browsers “preflight” the request, soliciting
     * supported methods from the server with an HTTP OPTIONS request
     */
    private boolean allowOptionsForCORS(HttpRequest request) {
        // in elasticsearch.yml set
        // http.cors.allow-headers: "X-Requested-With, Content-Type, Content-Length, Authorization"
        if (request.method() == Method.OPTIONS) {
//            Loggers.getLogger(getClass()).error("CORS type {}, address {}, path {}, request {}, content {}",
//                    request.method(), getAddress(request), request.path(), request.params(), request.content().toUtf8());
            return true;
        }
        return false;
    }
}
