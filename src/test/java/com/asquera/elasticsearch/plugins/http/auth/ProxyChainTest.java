package com.asquera.elasticsearch.plugins.http.auth;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;

public class ProxyChainTest {


  @Test
  public void emptyProxyChainGeneratesEmptySubchain() {
    assertTrue(new ProxyChain().subchains().isEmpty());
  }

  @Test
  public void notEmptyIsIncludedInSubchain() {
    ArrayList<String> c = new ArrayList<String>();
    c.add( "123.134.123.213" );
    ProxyChain r = new ProxyChain(c);
    assertTrue(r.subchains().contains(r));
  }

  @Test
  public void lastTwoIncludedInSubchain() {
    ArrayList<String> c = new ArrayList<String>();
    c.add( "1.1.1.1" );
    c.add( "3.3.3.3" );
    ProxyChain r = new ProxyChain(c);
    ProxyChain s = new ProxyChain();
    s.add( "1.1.1.1" );
    s.add( "3.3.3.3" );
    assertTrue(r.subchains().contains(s));
  }
  
  @Test
  public void firstTwoNotIncludedInSubchain() {
    ArrayList<String> c = new ArrayList<String>();
    c.add( "123.134.123.213" );
    c.add( "1.1.1.1" );
    c.add( "3.3.3.3" );
    ProxyChain r = new ProxyChain(c);
    ProxyChain s = new ProxyChain();
    s.add(c.get(0));
    s.add(c.get(1));
    assertFalse(r.subchains().contains(s));
  }

  @Test
  public void middleNotIncludedInSubchain() {
    ArrayList<String> c = new ArrayList<String>();
    c.add( "123.134.123.213" );
    c.add( "1.1.1.1" );
    c.add( "3.3.3.3" );
    ProxyChain r = new ProxyChain(c);
    ProxyChain s = new ProxyChain();
    s.add(c.get(1));
    assertFalse(r.subchains().contains(s));
  }
}
