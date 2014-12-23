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

import org.apache.commons.net.util.SubnetUtils;

/**
 *
 * Wraps the configured whitelisted ips.
 * Uses a Set of SubnetUtils objects.
 * <p>
 *
 *
 *
 * @author Ernesto Miguez (ernesto.miguez@asquera.de)
 * @author Nigel Foucha (nigel.foucha@gmail.com)
 */

public class InetAddressWhitelist {
  private static final String LOCALHOST = "127.0.0.1";
  private static final String SINGLEMASK = "255.255.255.255";
  private Set<SubnetUtils> whitelist;
  /**
   *
   *
   * @param whitelist
   */
  public InetAddressWhitelist(Set<SubnetUtils> whitelist) {
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
    return contains(candidate.getHostAddress());
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
    boolean result = false;
    for (SubnetUtils util : whitelist) {
      try {
        if (util.getInfo().isInRange(candidate)) {
          result = true;
          break;
        }
      } catch (IllegalArgumentException e) {
        Loggers.getLogger(InetAddressWhitelist.class).debug("Illegal address encountered {}, error: {}", candidate, e.getMessage());
      }
    }
    return new Boolean(result);
  }

  /**
   *
   * @param  ips a list of string ips
   * @return a list of {@link InetAddress} objects
   *
   */
  static Set<SubnetUtils> toInetAddress(List<String> ips) {
    List<SubnetUtils> listIps = new ArrayList<SubnetUtils>();
    for (String ip : ips) {
      SubnetUtils util = null;
      Loggers.getLogger(InetAddressWhitelist.class).debug("Processing ip entry: {}", ip);
      try {
        if ((ip == null) || (ip.length() <= 0)) {
          Loggers.getLogger(InetAddressWhitelist.class).debug("Empty address encountered, setting to localhost");
          InetAddress address = InetAddress.getByName(ip);
          util = new SubnetUtils(address.getHostAddress(), SINGLEMASK);
          util.setInclusiveHostCount(true);
          listIps.add(util);
        }
        else if (ip.indexOf('/') > -1) {
          util = new SubnetUtils(ip);
          util.setInclusiveHostCount(true);
          listIps.add(util);
        }
        else if (ip.indexOf(',') > -1) {
          String[] parts = ip.split(",");
          util = new SubnetUtils(parts[0], parts[1]);
          util.setInclusiveHostCount(true);
        }
        else {
          // Here we create a util for a single ip address or hostname
          InetAddress address = InetAddress.getByName(ip);
          util = new SubnetUtils(address.getHostAddress(), SINGLEMASK);
          util.setInclusiveHostCount(true);
          listIps.add(util);
        }
      } catch (IllegalArgumentException e) {
        String template = "an ip set in the whitelist settings raised an " +
          "IllegalArgumentException: {}, dropping it";
        Loggers.getLogger(InetAddressWhitelist.class).info(template, e.getMessage());
      } catch (UnknownHostException e) {
        String template = "an ip set in the whitelist settings raised an " +
          "UnknownHostException: {}, dropping it";
        Loggers.getLogger(InetAddressWhitelist.class).info(template, e.getMessage());
      }
    }
    return new HashSet<SubnetUtils>(listIps);
  }

  /**
   * delegate method
   */
  @Override
  public String toString() {
    return whitelist.toString();
  }

}
