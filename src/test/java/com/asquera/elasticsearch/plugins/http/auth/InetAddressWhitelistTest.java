package com.asquera.elasticsearch.plugins.http.auth;

import static org.junit.Assert.*;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressWhitelistTest {

  static final String localhost = "localhost";
  static final String containedIp = "1.1.1.1";
  static String notContainedIp = "2.2.2.2";
  private InetAddressWhitelist whitelist(String ip) {
    String[] w = { ip };
    return new InetAddressWhitelist(w);
  }

  @Test
  public void testInnetLocalhost() throws UnknownHostException {
    assertTrue(whitelist(localhost).contains(InetAddress.getByName(localhost)));
  }
  @Test
  public void testInnetNullDefaultsToLocalhost() throws UnknownHostException {
    assertTrue(whitelist(null).contains(InetAddress.getByName(localhost)));
  }
  @Test
  public void testStringLocalhostNotMatched() throws UnknownHostException {
    // the ip that "localhost" resolves to its matched ip and not the string
    // "localhost" itself
    assertFalse(whitelist(localhost).contains(localhost));
  }

  @Test
  public void testIpContained() throws UnknownHostException {
    assertTrue(whitelist(containedIp).contains(containedIp));
  }
  
  @Test
  public void testEmptyWhitelist() throws UnknownHostException {
    assertFalse(whitelist("").contains(notContainedIp));
  }

  @Test
  public void testNotContained() throws UnknownHostException {
    assertFalse(whitelist(containedIp).contains(notContainedIp));
  }

  @Test
  public void invalidIpIsDropped() throws UnknownHostException {
    String invalidIp = "555.555.555.555";
    assertFalse(whitelist(invalidIp).contains(invalidIp));
  }
}
