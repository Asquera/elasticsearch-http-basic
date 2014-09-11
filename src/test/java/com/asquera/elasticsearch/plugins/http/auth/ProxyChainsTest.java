package com.asquera.elasticsearch.plugins.http.auth;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

public class ProxyChainsTest {

  private ProxyChains trustedChains;
  private ProxyChain trustedCandidateChain;
  private ProxyChain unTrustedCandidateChain;
  private final String untrustedChain1 = "50.50.50.50";
  private final String trustedChain1 = "7.7.7.7";
  private final String trustedChain2 = "5.5.5.5,6.6.6.6";
  private final String trustedChain3 = "8.8.8.8,9.9.9.9,10.10.10.10";
  private final String trustedChain4 = "2.2.2.2" + "," + trustedChain2;
  private final String[] t = { trustedChain1, trustedChain2, trustedChain3, trustedChain4 };

  @Before public void initialize() {
    trustedCandidateChain = new ProxyChain(trustedChain1);
    unTrustedCandidateChain = new ProxyChain(untrustedChain1);
    trustedChains = new ProxyChains(t);
  }

  @Test(expected=NullPointerException.class)
  public void NullPointerExceptionInNullTrustedIps() {
    String[] t = null;
    trustedChains = new ProxyChains(t);
    assertFalse(trustedChains.trusts(trustedCandidateChain));
  }

  @Test
  public void unTrustsEmptyCandidate() {
    assertFalse(trustedChains.trusts(new ProxyChain("")));
  }

  @Test
  public void unTrustsAnyCandidateWithEmptyTrustedChain() {
    String[] t = {""};
    trustedChains = new ProxyChains(t);
    assertFalse(trustedChains.trusts(trustedCandidateChain));
  }

  @Test
  public void trustsCandidatesInProxyChain() {
    assertTrue(trustedChains.trusts(trustedCandidateChain));
  }

  @Test
  public void unTrustedCandidateNotInProxyChain() {
    assertFalse(trustedChains.trusts(unTrustedCandidateChain));
  }

  @Test
  public void trustCandidateContainedInTwoTrustedChains() {
    trustedCandidateChain = new ProxyChain(trustedChain2);
    assertTrue(trustedChains.trusts(trustedCandidateChain));
  }
}
