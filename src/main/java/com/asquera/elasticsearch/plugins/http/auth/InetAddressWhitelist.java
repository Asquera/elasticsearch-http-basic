package com.asquera.elasticsearch.plugins.http.auth;
import org.elasticsearch.common.logging.Loggers;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Arrays;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * Wraps the configured whitelisted ips.
 * It uses a set of {@link InetAddress} internally.
 * <p>
 *
 *
 *
 * @author Ernesto Miguez (ernesto.miguez@asquera.de)
 */

public class InetAddressWhitelist {
  private Set<InetAddress> whitelist;
  /**
   *
   *
   * @param whitelist
   */
  public InetAddressWhitelist(Set<InetAddress> whitelist) {
    this.whitelist = whitelist;
  }

  /**
   *
   *
   * @param sWhitelist
   *
   */
  public InetAddressWhitelist(String[] sWhitelist) {
    this(toInetAddress(Arrays.asList(sWhitelist)));
  }

  /**
   * Checks the request ip for inclusion.
   * Since that ip comes in a {@link InetAddress} representation, it is checked
   * against the whitelist.
   *
   * @param candidate
   * @return if the ip is included in the whitelist
   */
  public Boolean contains(InetAddress candidate) {
    return this.whitelist.contains(candidate);
  }

  /**
   *
   * Checks the xForwardedFor defined client ip for inclusion.
   * Since that ip comes in a String representation, it is checked against
   * the String representation of the defined whitelist.
   *
   * @param candidate
   * @return if the ip is included in the String representation of the
   * whitelist ips
   */
  public Boolean contains(String candidate) {
    return getStringWhitelist().contains(candidate);
  }

  /**
   * @return set of the string representations of the whitelist
   */
  Set<String> getStringWhitelist() {
    Iterator<InetAddress> iterator = this.whitelist.iterator();
    Set<String> set = new HashSet<String>();
    while (iterator.hasNext()) {
      InetAddress next = iterator.next();
      set.add(next.getHostAddress());
    }
    return set;
  }

  /**
   * when an configured InetAddress is Unkown or Invalid it is dropped from the
   * whitelist
   *
   * @param  ips a list of string ips
   * @return a list of {@link InetAddress} objects
   *
   */
  static Set<InetAddress> toInetAddress(List<String> ips) {
    List<InetAddress> listIps = new ArrayList<InetAddress>();
    Iterator<String> iterator = ips.iterator();
    while (iterator.hasNext()) {
      String next = iterator.next();
      try {
        listIps.add(InetAddress.getByName(next));
      } catch (UnknownHostException e) {
        String template = "an ip set in the whitelist settings raised an " +
          "UnknownHostException: {}, dropping it";
        Loggers.getLogger(InetAddressWhitelist.class).info(template, e.getMessage());
      }
    }
    return new HashSet<InetAddress>(listIps);
  }

  /**
   * delegate method
   */
  @Override
  public String toString() {
    return whitelist.toString();
  }

}
