package com.asquera.elasticsearch.plugins.http.auth;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 *
 * Class that handles the values obtained from the X-Forwarded-For (XFF) HTTP Header
 * field.
 * <p>
 * The X-Forwarded-For (XFF) HTTP header field is a de facto standard for
 * identifying the originating IP address of a client connecting to a web
 * server through an HTTP proxy or load balancer.
 * <p>
 * The usefulness of XFF depends on the proxy server truthfully reporting the
 * original host's IP address; for this reason, effective use of XFF requires
 * knowledge of which proxies are trustworthy, for instance by looking them
 * up in a whitelist of servers whose maintainers can be trusted.
 *
 * @see <a href="http://en.wikipedia.org/wiki/X-Forwarded-For">X-Forwarded-For</a>
 *
 *
 *
 * @author Ernesto Miguez (ernesto.miguez@asquera.de)
 */

public class XForwardedFor {
  /**
   *
   * The X-Forwarded-For Header value as received in the request
   * The general format of the field is:
   *
   *     X-Forwarded-For: client, proxy1, proxy2
   */
  private final String xForwardedFor;

  /**
   *
   * @param xForwardedFor
   */
  public XForwardedFor(String xForwardedFor) {
    this.xForwardedFor = xForwardedFor != null ? xForwardedFor : "";
  }

  /**
   * @return the ip of the client as defined by the X-Forwarded-For Header
   */
  public String client() {
    ArrayList<String> splitted_ips =  new ArrayList<String>(
        Arrays.asList(xForwardedFor.split(",")));
    return splitted_ips.remove(0);
  }

  /**
   *
   * @return true if the X-Forwarded-For header was set
   */
  public boolean isSet() {
    return ! xForwardedFor.equals("");
  }

  /**
   *  delegate method
   */
  @Override
  public String toString() {
    String s = "not used";
    if (isSet()) {
      s = xForwardedFor;
    }
    return s;
  }

  /**
   * @return the ips of the proxies between the client(as defined by the
   * X-Forwarded-For Header) and the * server
   */
  protected List<String> proxies() {
    ArrayList<String> splitted_ips =  new ArrayList<String>(
        Arrays.asList(xForwardedFor.split(",")));
    splitted_ips.remove(0);
    return splitted_ips;
  }

  /**
   * @return the xForwardedFor
   */
  public String getxForwardedFor() {
    return xForwardedFor;
  }
}
