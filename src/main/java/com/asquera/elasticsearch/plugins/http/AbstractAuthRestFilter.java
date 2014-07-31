package com.asquera.elasticsearch.plugins.http;

import static org.elasticsearch.rest.RestStatus.OK;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestFilter;
import org.elasticsearch.rest.RestFilterChain;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestRequest.Method;

//# possible http config
//http.basic.user: admin
//http.basic.password: password
//http.basic.ipwhitelist: ["localhost", "somemoreip"]
//http.basic.xforward: "X-Forwarded-For"
//# if you use javascript
//# EITHER $.ajaxSetup({ headers: { 'Authorization': "Basic " + credentials }});
//# OR use beforeSend in  $.ajax({
//http.cors.allow-headers: "X-Requested-With, Content-Type, Content-Length, Authorization"
//
/**
* @author Florian Gilcher (florian.gilcher@asquera.de)
* @author Peter Karich
*/
public abstract class AbstractAuthRestFilter extends RestFilter {

	protected final String user;
	protected final String password;
	protected final Set<String> whitelist;
	protected final String xForwardFor;
	protected final boolean log;
	protected final ESLogger logger;

	protected AbstractAuthRestFilter(Settings settings) {
		this.user = settings.get("http.basic.user", "admin");
        this.password = settings.get("http.basic.password", "admin_pw");
        this.whitelist = new HashSet<String>(Arrays.asList(
                settings.getAsArray("http.basic.ipwhitelist",
                new String[]{"localhost", "127.0.0.1"})));

        // for AWS load balancers it is X-Forwarded-For -> hmmh does not work
        this.xForwardFor = settings.get("http.basic.xforward", "");
        this.log = settings.getAsBoolean("http.basic.log", false);
        this.logger = Loggers.getLogger(getClass(), settings);
        this.logger.info("using {}:{} with whitelist {}, xforward {}",
                user, password, whitelist, xForwardFor);
	}

	@Override
	public void process(RestRequest request, RestChannel channel,
			RestFilterChain filterChain) throws Exception {
        if (log) {
            logger.info("Authorization:{}, host:{}, xforward:{}, path:{}, isInWhitelist:{}, Client-IP:{}, X-Client-IP:{}",
                    request.header("Authorization"), request.header("host"),
                    request.header(xForwardFor), request.path(), isInIPWhitelist(request),
                    request.header("X-Client-IP"), request.header("Client-IP"));
        }
        // allow health check even without authorization
        if (allowOptionsForCORS(request) || isInIPWhitelist(request) || isAuthenticated(request)) {
        	  filterChain.continueProcessing(request, channel);
        } else if (healthCheck(request)) {
            channel.sendResponse(new BytesRestResponse(OK, "{\"OK\":{}}"));
        } else {
        	  sendAuthenticationChallenge(request, channel);
        }
	}

	protected abstract boolean isAuthenticated(RestRequest request);

	protected abstract void sendAuthenticationChallenge(RestRequest request, RestChannel channel);

	protected boolean healthCheck(final RestRequest request) {
        String path = request.path();
        return (request.method() == RestRequest.Method.GET) && path.equals("/");
    }

    protected String getHostAndPort(RestRequest request) {
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
        return addr;
    }
    protected String getAddress(RestRequest request) {
        String addr = getHostAndPort(request);
        int portIndex = addr.indexOf(":");
        if (portIndex >= 0)
            addr = addr.substring(0, portIndex);
        return addr;
    }

    protected boolean isInIPWhitelist(RestRequest request) {
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
    protected boolean allowOptionsForCORS(RestRequest request) {
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
