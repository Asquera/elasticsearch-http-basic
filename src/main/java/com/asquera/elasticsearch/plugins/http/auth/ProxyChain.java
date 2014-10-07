package com.asquera.elasticsearch.plugins.http.auth;

import java.util.*;

/**
 *
 *
 * This class wraps an ip chain (an ordered list of ips).
 *
 * @author Ernesto Miguez (ernesto.miguez@asquera.de)
 *
 */
public class ProxyChain {
  private List <String> proxyChain;

  public ProxyChain() {
    this.proxyChain = new ArrayList<String>();
  }

  public ProxyChain(List<String> proxyChain) {
    this.proxyChain = proxyChain;
  }

  public ProxyChain(String proxyChain) {
    this(new ArrayList<String>(Arrays.asList(proxyChain.split(","))));
  }


  /**
   * @return the proxy chain
   */
    public List<String> getProxyChain() {
      return proxyChain;
    }

  /**
   * <b>A subchain</b> is every segment of the list matching <b>by the tail</b>, included
   * itself. <p><b> example: </b>
   * <span>"1.1.1.1,2.2.2.2" is trusted by trusted list "3.3.3.3,4.4.4.4,2.2.2.2" since the
   * subchain "2.2.2.2" is included in a subchain of the trusted list. </span>
   *
   * @return a new {@link ProxyChain} instance having all the subchains of the
   * present instance
   */
  public ProxyChains subchains() {
    List<String> reversedIps = new ArrayList<String>(proxyChain);
    Collections.reverse(reversedIps);
    ListIterator<String> iterator = reversedIps.listIterator();
    ProxyChains subchains = new ProxyChains((Set<ProxyChain>)new HashSet<ProxyChain>());
    ProxyChain subChain = new ProxyChain(new ArrayList<String>());
    while (iterator.hasNext()) {
      String next = iterator.next();
      subChain.add(next);
      List<String> r =  new ArrayList<String>(subChain.getProxyChain());
      Collections.reverse(r);
      subchains.add( new ProxyChain(r));
    }
    return subchains;
  }

  /**
   * delegated method
   * @param o
   * @see List#add(Object o);
   */
  public void add (Object o) {
    proxyChain.add((String)o);
  }

  /**
   * delegated method
   * @see List#toString();
   */
  @Override
  public String toString() {
    return proxyChain.toString();
  }

  /**
   * delegated method
   * @see List#equals();
   */
  @Override
  public boolean equals(Object c) {
    return proxyChain.equals(((ProxyChain)c).getProxyChain());
  }

  /**
   * delegated method
   * @see List#hashCode();
   */
  @Override
  public int hashCode() {
    return proxyChain.hashCode();
  }

  /**
   * delegated method
   * @see List#size();
   */
  public int size() {
    return proxyChain.size();
  }
}
