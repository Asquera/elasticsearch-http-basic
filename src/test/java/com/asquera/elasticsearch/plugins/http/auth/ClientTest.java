package com.asquera.elasticsearch.plugins.http.auth;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;

import org.junit.Test;

import java.net.UnknownHostException;
import java.net.InetAddress;


public class ClientTest{

  private final String whitelistedIp = "8.8.8.8";
  private final String notWhitelistedIp = "42.42.42.42";
  private final String untrustedRequestIp = "50.50.50.50";
  private final String trustedRequestIp = "6.6.6.6";
  private String[] trustedIps = {"7.7.7.7,"+ trustedRequestIp};
  private String[] whitelist   = { whitelistedIp };
  private String xForwardedFor =  "9.9.9.9,8.8.8.8,7.7.7.7";

	@Test
	public void authorizedWhitelistedRequestNilXForwardedFor() throws UnknownHostException {
    Client c = new Client(
      InetAddress.getByName(whitelistedIp),
      new InetAddressWhitelist(whitelist),
      new XForwardedFor(null),
      new ProxyChains(trustedIps));
      assertThat(c.ip(), is(whitelistedIp));
      assertTrue(c.isTrusted());
      assertTrue(c.isWhitelisted());
      assertTrue(c.isAuthorized());
  }

	public void authorizedWhitelistedRequestUnsetProxies() throws UnknownHostException {
    Client c = new Client(
      InetAddress.getByName(whitelistedIp),
      new InetAddressWhitelist(whitelist),
      new XForwardedFor(""),
      new ProxyChains(trustedIps));
      assertThat(c.ip(), is(whitelistedIp));
      assertTrue(c.isTrusted());
      assertTrue(c.isWhitelisted());
      assertTrue(c.isAuthorized());
  }

	@Test
	public void unauthorizedWhitelistedRequestUnsetProxies() throws UnknownHostException {
    Client c = new Client(
      InetAddress.getByName(notWhitelistedIp),
      new InetAddressWhitelist(whitelist),
      new XForwardedFor(""),
      new ProxyChains(trustedIps));
      assertThat(c.ip(), is(notWhitelistedIp));
      assertTrue(c.isTrusted());
      assertFalse(c.isWhitelisted());
      assertFalse(c.isAuthorized());
  }

	@Test
	public void ipOfUntrustedRequestViaProxiesIsFirstOfXForwardedFor() throws UnknownHostException {
    Client c = new Client(
      InetAddress.getByName(untrustedRequestIp),
      new InetAddressWhitelist(whitelist),
      new XForwardedFor(xForwardedFor),
      new ProxyChains(trustedIps));
      assertThat(c.ip(), is("9.9.9.9"));
      assertFalse(c.isTrusted());
      assertFalse(c.isWhitelisted());
      assertFalse(c.isAuthorized());
  }

  @Test
	public void ipOfTrustedRequestViaProxiesIsInXForwardedFor() throws UnknownHostException {
    Client c = new Client(
      InetAddress.getByName(trustedRequestIp),
      new InetAddressWhitelist(whitelist),
      new XForwardedFor(xForwardedFor),
      new ProxyChains(trustedIps));
      assertThat(c.ip(), is("8.8.8.8"));
      assertTrue(c.isTrusted());
      assertTrue(c.isWhitelisted());
      assertTrue(c.isAuthorized());
  }

  @Test
	public void ipOfNotWhitelistedIpViaTrustedProxiesIsFirstOfXForwardedFor() throws UnknownHostException {
    String[] whitelist   = {"10.10.10.10"};
    Client c = new Client(
      InetAddress.getByName(trustedRequestIp),
      new InetAddressWhitelist(whitelist),
      new XForwardedFor(xForwardedFor),
      new ProxyChains(trustedIps));
    assertThat(c.ip(), is("8.8.8.8"));
    assertTrue(c.isTrusted());
    assertFalse(c.isWhitelisted());
    assertFalse(c.isAuthorized());
  }

  @Test
	public void noXForwardedSetRequestIpWhitelisted() throws UnknownHostException {
    Client c = new Client(
      InetAddress.getByName(whitelistedIp),
      new InetAddressWhitelist(whitelist),
      new XForwardedFor(""),
      new ProxyChains(trustedIps));
    assertThat(c.ip(), is(whitelistedIp));
    assertTrue(c.isTrusted());
    assertTrue(c.isWhitelisted());
    assertTrue(c.isAuthorized());
  }

  @Test
	public void noXForwardedSetRequestIpNotWhitelisted() throws UnknownHostException {
    Client c = new Client(
      InetAddress.getByName(notWhitelistedIp),
      new InetAddressWhitelist(whitelist),
      new XForwardedFor(""),
      new ProxyChains(trustedIps));
    assertThat(c.ip(), is(notWhitelistedIp));
    assertTrue(c.isTrusted());
    assertFalse(c.isWhitelisted());
    assertFalse(c.isAuthorized());
  }

  @Test
	public void clientIsTrustedBySeveralProxyChains() throws UnknownHostException {

    String[] trustedIps = {"1.1.1.1,2.2.2.2,3.3.3.3","4.4.4.4,2.2.2.2,3.3.3.3"};
    String xForwardedFor = whitelistedIp + ",2.2.2.2";
    Client c = new Client(
      InetAddress.getByName("3.3.3.3"),
      new InetAddressWhitelist(whitelist),
      new XForwardedFor(xForwardedFor),
      new ProxyChains(trustedIps));
    assertThat(c.ip(), is(whitelistedIp));
    assertTrue(c.isTrusted());
    assertTrue(c.isWhitelisted());
    assertTrue(c.isAuthorized());
  }

  @Test
	public void clientIsUntrustedAndInWhitelist() throws UnknownHostException {
    String xForwardedFor = whitelistedIp + ",2.2.2.2";
    Client c = new Client(
      InetAddress.getByName(untrustedRequestIp),
      new InetAddressWhitelist(whitelist),
      new XForwardedFor(xForwardedFor),
      new ProxyChains(trustedIps));
    assertThat(c.ip(), is(whitelistedIp));
    assertFalse(c.isTrusted());
    assertTrue(c.isWhitelisted());
    assertFalse(c.isAuthorized());
  }

  @Test
	public void clientIsTrustedAndInWhitelistViaOneProxy() throws UnknownHostException {
    String xForwardedFor = whitelistedIp;
    Client c = new Client(
      InetAddress.getByName(trustedRequestIp),
      new InetAddressWhitelist(whitelist),
      new XForwardedFor(xForwardedFor),
      new ProxyChains(trustedIps));
    assertThat(c.ip(), is(whitelistedIp));
    assertTrue(c.isTrusted());
    assertTrue(c.isWhitelisted());
    assertTrue(c.isAuthorized());
  }

  @Test
	public void clientIsUntrustedAndInWhitelistViaOneProxy() throws UnknownHostException {
    String xForwardedFor = whitelistedIp;
    Client c = new Client(
      InetAddress.getByName(untrustedRequestIp),
      new InetAddressWhitelist(whitelist),
      new XForwardedFor(xForwardedFor),
      new ProxyChains(trustedIps));
    assertThat(c.ip(), is(whitelistedIp));
    assertFalse(c.isTrusted());
    assertTrue(c.isWhitelisted());
    assertFalse(c.isAuthorized());
  }

  @Test
	public void lastXForwardTrustedProxyIsNotwhitelistedClient() throws UnknownHostException {
    String[] trustedIps = {"3.3.3.3,2.2.2.2,1.1.1.1," + trustedRequestIp};
    String xForwardedFor = notWhitelistedIp + "," + whitelistedIp + ",3.3.3.3,2.2.2.2";
    Client c = new Client(
      InetAddress.getByName(trustedRequestIp),
      new InetAddressWhitelist(whitelist),
      new XForwardedFor(xForwardedFor),
      new ProxyChains(trustedIps));
    assertThat(c.ip(), is("2.2.2.2"));
    assertTrue(c.isTrusted());
    assertFalse(c.isWhitelisted());
    assertFalse(c.isAuthorized());
  }

  @Test
	public void lastXForwardTrustedProxyIsWhitelistedClient() throws UnknownHostException {
    String[] trustedIps = {"3.3.3.3,2.2.2.2," + trustedRequestIp};
    String xForwardedFor = notWhitelistedIp + "," + whitelistedIp + ",2.2.2.2";
    Client c = new Client(
      InetAddress.getByName(trustedRequestIp),
      new InetAddressWhitelist(whitelist),
      new XForwardedFor(xForwardedFor),
      new ProxyChains(trustedIps));
    assertThat(c.ip(), is(whitelistedIp));
    assertTrue(c.isTrusted());
    assertTrue(c.isWhitelisted());
    assertTrue(c.isAuthorized());
  }


  @Test
	public void longestTrustedChainDefinesClientNontInWhitelist() throws UnknownHostException {
    String xForwardedFor =  notWhitelistedIp + "," + whitelistedIp + ",2.2.2.2";
    String[] trustedIps = { xForwardedFor + "," + trustedRequestIp};
    Client c = new Client(
      InetAddress.getByName(trustedRequestIp),
      new InetAddressWhitelist(whitelist),
      new XForwardedFor(xForwardedFor),
      new ProxyChains(trustedIps));
    assertThat(c.ip(), is(notWhitelistedIp));
    assertTrue(c.isTrusted());
    assertFalse(c.isWhitelisted());
    assertFalse(c.isAuthorized());
  }
}
