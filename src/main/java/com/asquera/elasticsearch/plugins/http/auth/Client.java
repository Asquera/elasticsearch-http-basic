package com.asquera.elasticsearch.plugins.http.auth;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for determining the ip of the
 * remote client and if the client is authorized based on its ip.
 * a client is authorized iff it's ip is whitelisted and the path to the
 * server is trusted
 * <p>
 * a client can be trusted in this cases:
 * <ul>
 * <li>client is directly connected to the server and its request ip is
 * whitelisted
 * <li>client is connected to the server via a chain of proxies trusted by
 * the server, and its ip is whitelisted
 *
 * @author Ernesto Miguez (ernesto.miguez@asquera.de)
 */
public class Client {
	private final InetAddress requestIp;
	private final InetAddressWhitelist whitelist;
	private final XForwardedFor xForwardedFor;
	private final ProxyChains trustedProxyChains;
	/**
	 * the trusted state of the client. A client is trusted if it is connected
	 * directly or if it is connected via a trusted ip chain.
	 */
	private boolean trusted;
	/**
	 * the whitelisted state of the client.
	 */
	private boolean whitelisted;
	/**
	 * the authorize state of the client.
	 * true iff the client it is conected via a trusted proxy chain and
	 * its client ip is whitelisted:
	 */
	private boolean authorized;

	/**
	 * @param requestIp
	 * @param whitelist
	 * @param xForwardedFor
	 * @param trustedProxyChains
	 * @param authorized
	 * @param trusted
	 * @param whitelisted
	 */
	public Client(InetAddress requestIp, InetAddressWhitelist whitelist,
								XForwardedFor xForwardedFor, ProxyChains trustedProxyChains) {
		this.requestIp = requestIp;
		this.whitelist = whitelist;
		this.xForwardedFor = xForwardedFor;
		this.trustedProxyChains = trustedProxyChains;
		trusted = checkTrusted();
		whitelisted = checkWhitelisted();
		authorized = trusted && whitelisted;
	}

	/**
	 * @return the String representation of the client's ip
	 * from the point of view of the server
	 * <p>
	 * this can be one of:
	 * <ul>
	 * <li> client ip from X-Forwarded-For
	 * <li> a proxy ip in the proxy chain defined in X-Forwarded-For
	 * <li> the request ip
	 */
	public String ip() {
		String ip = requestIp.getHostAddress();
		if (xForwardedFor.isSet()) {
			ip = remoteClientIp();
		}
		return ip;
	}

	/**
	 * determines the trust state of the client.
	 * <p>
	 * The client is trusted when:
	 * <ul>
	 * <li> it is not connected via proxy
	 * <li> it is connected via proxies and least one of the proxies subchains is trusted
	 *
	 * @return true if the client's proxy chain is trusted or if the client is
	 * not connected via proxy, false otherwise.
	 */

	private boolean checkTrusted() {
		boolean trusted = true;
		if (xForwardedFor.isSet()) {
			trusted = trustedProxyChains.trusts(requestChain());
		}
		return trusted;
	}

	/**
	 * If the client conects via proxy its ip can be <b>any<b/> of the listed
	 * in the X-Forwarded-For field; <b>the trusted subchain determines which is the
	 * client's remote ip, as the ip previous to the first item of the trusted
	 * subchain</b>.
	 * <p>
	 * If the client doesn't connect via proxy its ip is the request ip
	 *
	 * @return true iff clients ip is whitelisted
	 */
	private boolean checkWhitelisted() {
		boolean whitelisted = false;
		if (xForwardedFor.isSet()) {
			whitelisted = whitelist.contains(remoteClientIp());
		} else {
			whitelisted = whitelist.contains(requestIp);
		}
		return whitelisted;
	}

	/**
	 * @return the trusted ip chain or null if none
	 */
	private ProxyChain trustedChain() {
		return trustedProxyChains.trustedSubchain(requestChain());
	}

	/**
	 * @param request_ip
	 * @return an request chain in the form of [proxy-1, .., proxy-n, request]
	 */
	private ProxyChain requestChain() {
		List<String> ipsChain = new ArrayList<String>();
		ipsChain.addAll(xForwardedFor.proxies());
		ipsChain.add(requestIp.getHostAddress());
		return new ProxyChain(ipsChain);
	}

	/**
	 * In case of X-Forwarded-For set, the ip of the remote client relative
	 * to the server is defined as follows:
	 * <p>
	 * by default the remote client ip is the first ip of the X-Forwarded-For.
	 * If there is a sub proxy chain in the X-Forwarded-For that is trusted, the
	 * client ip is the ip that precedes the starting of the trusted subchain.<p>
	 * example:<p>
	 * <p>
	 * a X-Forwarded-For value "1.1.1.1,2.2.2.2,3.3.3.3" with "3.3.3.3" as
	 * trusted proxy chain will have the "3.3.3.3" subchain trusted. This
	 * determines "2.2.2.2" as the server's remote client
	 *
	 * @return the remote client's ip relative to the server
	 */
	private String remoteClientIp() {
		String clientIp = xForwardedFor.client();
		if (trustedChain() != null) {
			List<String> trustedProxies = trustedChain().getProxyChain();
			List<String> proxies = xForwardedFor.proxies();
			proxies.removeAll(trustedProxies);
			if (proxies.size() > 0) {
				clientIp = proxies.get(proxies.size() - 1);
			}
		}
		return clientIp;
	}

	@Override
	public String toString() {
		String addr = requestIp.getHostAddress();
		String s = "client with request ip " + addr
						+ (xForwardedFor.isSet() ? ", remoteIp: " + remoteClientIp() : "")
						+ " is:"
						+ (authorized ? "Authorized" : "NotAuthorized")
						+ ", "
						+ (trusted ? "Trusted" : "NotTrusted")
						+ " (X-Forwarded-For: "
						+ xForwardedFor
						+ "), "
						+ (whitelisted ? "Whitelisted" : "NotWhitelisted");
		return s;
	}

	/**
	 * @return the trusted
	 */
	public boolean isTrusted() {
		return trusted;
	}

	/**
	 * @return the whitelisted
	 */
	public boolean isWhitelisted() {
		return whitelisted;
	}

	/**
	 * @return the authorized
	 */
	public boolean isAuthorized() {
		return authorized;
	}
}
