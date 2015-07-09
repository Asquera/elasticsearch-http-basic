package com.asquera.elasticsearch.plugins.http.auth;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.elasticsearch.common.logging.Loggers;

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
  private Set<Object> whitelist;
  /**
   *
   *
   * @param whitelist
   */
  public InetAddressWhitelist(Set<Object> whitelist) {
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
    if (this.whitelist.contains(candidate)){
    	return true;
    }
    
    //We also need to itterate through each of the patterns to make sure it doesn't match there
    for (Object obj : whitelist){
   	 if (obj.getClass() == Pattern.class){
   		 Pattern pattern = (Pattern)obj;
   		 if (pattern.matcher(candidate.getHostAddress()).matches()){
   			 return true;
   		 }
   	 }
    }
    
    return false;
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
     if (getStringWhitelist().contains(candidate)){
    	 return true;
     }
     
     //We also need to itterate through each of the patterns to make sure it doesn't match there
     for (Object obj : whitelist){
    	 if (obj.getClass() == Pattern.class){
    		 Pattern pattern = (Pattern)obj;
    		 if (pattern.matcher(candidate).matches()){
    			 return true;
    		 }
    	 }
     }
     
     return false;
  }

  /**
   * @return set of the string representations of the whitelist
   */
  Set<String> getStringWhitelist() {
    Iterator<Object> iterator = this.whitelist.iterator();
    Set<String> set = new HashSet<String>();
    while (iterator.hasNext()) {
      Object next = iterator.next();
      if (next.getClass() == Pattern.class){
    	  set.add(next.toString());
      } 
      else{
    	  InetAddress add = (InetAddress)next;
    	  set.add(add.getHostAddress());
      }
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
  static Set<Object> toInetAddress(List<String> ips) {
    List<Object> listIps = new ArrayList<Object>();
    Iterator<String> iterator = ips.iterator();
    while (iterator.hasNext()) {
      String next = iterator.next();
      if (next != null && next.startsWith("~")){
    	 Pattern pattern = Pattern.compile(next.substring(1));
    	 listIps.add(pattern);
      }
      else {
	      try {
	        listIps.add(InetAddress.getByName(next));
	      } catch (UnknownHostException e) {
	        String template = "an ip set in the whitelist settings raised an " +
	          "UnknownHostException: {}, dropping it";
	        Loggers.getLogger(InetAddressWhitelist.class).info(template, e.getMessage());
	      }
      }
    }
    return new HashSet<Object>(listIps);
  }

  /**
   * delegate method
   */
  @Override
  public String toString() {
    return whitelist.toString();
  }

}