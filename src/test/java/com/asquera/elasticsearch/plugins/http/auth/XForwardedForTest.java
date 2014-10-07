package com.asquera.elasticsearch.plugins.http.auth;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class XForwardedForTest {

  @Test
  public void returnsClientWithClientAndProxy() {
    // It seems the getName lookup for empty string is localhost
    String xForwardedFor = "123.123.123.123,122.122.12.1" ;
    assertThat(new XForwardedFor(xForwardedFor).client(),
        is(xForwardedFor.split(",")[0]));
  }
  
  @Test
  public void returnsClientWithClient() {
    // It seems the getName lookup for empty string is localhost
    String xForwardedFor = "123.123.123.123" ;
    assertThat(new XForwardedFor(xForwardedFor).client(),
        is(xForwardedFor.split(",")[0]));
  }

  @Test
  public void returnsClientWithNil() {
    assertThat(new XForwardedFor(null).client(), is(""));
  }

  @Test
  public void unsetHeaderReturnsEmptyClient() {
    // It seems the getName lookup for empty string is localhost
    String xForwardedFor = "" ;
    assertFalse(new XForwardedFor(xForwardedFor).isSet());
  }
}
