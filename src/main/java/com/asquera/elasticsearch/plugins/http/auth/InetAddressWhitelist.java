package com.asquera.elasticsearch.plugins.http.auth;
import org.elasticsearch.common.logging.Loggers;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * Wraps the configured whitelisted ips.
 * It uses a set of {@link Pattern} internally.
 * <p>
 *
 *
 *
 * @author Matthew Knox
 */

public class InetAddressWhitelist {
  private Set<Pattern> whitelist;
  
  /**
   *
   *
   * @param whitelist
   */
  public InetAddressWhitelist(Set<Pattern> whitelist) {
    this.whitelist = whitelist;
  }

  /**
   *
   *
   * @param sWhitelist
   *
   */
  public InetAddressWhitelist(String[] sWhitelist) {
    this(toPattern(Arrays.asList(sWhitelist)));
  }

  /**
   * Checks the request ip for inclusion.
   * Since that ip comes in a {@link InetAddress} representation, the IP 
   * string is grabbed and compared to the patterns.
   *
   * @param candidate
   * @return if the ip is included in the whitelist
   */
  public Boolean contains(InetAddress candidate) {
	return contains(candidate.getHostAddress());
  }

  /**
   *
   * Checks a defined client ip for inclusion.
   * Since that ip comes in a String representation, it is matched against
   * the patterns defined in the whitelist.
   *
   * @param candidate
   * @return if the ip is included in the String representation of the
   * whitelist ips
   */
  public Boolean contains(String candidate) {
	   	for (Pattern pattern : whitelist){
	   		if (pattern.matcher(candidate).matches()){
	   			return true;
	   		}
	   	}  
	   	return false;
  }

  /**
   * @return set of the string representations of the whitelist
   */
  Set<String> getStringWhitelist() {
    Iterator<Pattern> iterator = this.whitelist.iterator();
    Set<String> set = new HashSet<String>();
    while (iterator.hasNext()) {
      Pattern next = iterator.next();
      set.add(next.toString());
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
  static Set<Pattern> toPattern(List<String> ips) {
    List<Pattern> listIps = new ArrayList<Pattern>();
    Iterator<String> iterator = ips.iterator();
    while (iterator.hasNext()) {
      String next = iterator.next();
      try {
        listIps.add(Pattern.compile(next));
      } catch (Exception e) {
        String template = "an ip set in the whitelist settings raised an Exception, dropping it:" +
           e.getMessage();
        Loggers.getLogger(InetAddressWhitelist.class).info(template, e.getMessage());
      }
    }
    return new HashSet<Pattern>(listIps);
  }

  /**
   * delegate method
   */
  @Override
  public String toString() {
    return whitelist.toString();
  }

}
