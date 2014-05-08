package com.asquera.elasticsearch.plugins.http.auth;

import java.util.*;

/**
 *  This class wraps a set of {@link ProxyChain} 
 *
 * @author Ernesto Miguez (ernesto.miguez@asquera.de)
 **/

public class ProxyChains {

  private Set<ProxyChain> proxyChains;

  public ProxyChains(Set<ProxyChain> proxyChains) {
    this.proxyChains = proxyChains;
  }

  public ProxyChains(String[] proxyChains) {
    this(getProxies(proxyChains));
  }

  /**
   * 
   * An ip chain is <b>trusted</b> iff any of it subchains is contained in 
   * any of the instance subchains
   *
   * @param candidate the ip list to check
   * @return true iff the candidate is included
   */
  public Boolean trusts(ProxyChain candidate) {
    return trustedSubchain(candidate) != null;
  }

  /**
   *
   * Find the trusted subchain if any.<p> <b>note:</b> Any chain is included in its subchains
   *
   * @param candidate 
   * @return the trusted subchain or nil if none is trusted.
   * If more than one is trusted, the <b>longuest</b> will be returned
   */
  public ProxyChain trustedSubchain(ProxyChain candidate) {
    Set<ProxyChain> sub = subchains();
    sub.retainAll(candidate.subchains().getProxyChains());
    ProxyChain trusted = null;
    if (!sub.isEmpty()) {
      trusted = Collections.max(sub, new InetAddressChainComparator()); 
    }
    return trusted;
  }

  /**
   * a comparator that uses ip chain size
   */
  class InetAddressChainComparator implements Comparator<ProxyChain> {
    @Override
    public int compare(ProxyChain a, ProxyChain b) {
      return a.size() < b.size() ? -1 : a.size() == b.size() ? 0 : 1;
    }
  }

  /**
   *
   * @return the set of subchains of the trusted ip proxy chains
   * @see {@link ProxyChain#subchains()}
   */

  public Set<ProxyChain> subchains() {
    Iterator<ProxyChain> iterator = proxyChains.iterator();
    Set<ProxyChain> set = new HashSet<ProxyChain>();
    while (iterator.hasNext()) {
      ProxyChain next = iterator.next();
      set.addAll(next.subchains().getProxyChains());
    }
    return set;
  }

  /**
   *
   * delegated method
   *
   */
  public boolean contains(Object c) {
    return proxyChains.contains(c);
  }


  /**
   *
   * delegated method
   *
   * @param chain
   * @return if it is empty
   */
  public boolean isEmpty() {
    return proxyChains.isEmpty();
  }

  /**
   *
   * delegated method
   *
   * @param chain
   * @return true if it could be added
   */
  public boolean add(ProxyChain chain) {
    return proxyChains.add(chain);
  }


  /**
   *
   * delegated method
   */
  @Override
  public String toString() {
    return proxyChains.toString();
  }

  /**
   *
   * @param array of proxies represented as comma separated strings
   * @return a {@link ProxyChain} object representing the passed proxies
   *
   */

  private static Set<ProxyChain> getProxies(String[] ips) {
    Set<ProxyChain> pChainSet = new HashSet<ProxyChain>();
    Iterator<String> iterator = (Arrays.asList(ips)).iterator();
    while (iterator.hasNext()) {
      String next = iterator.next();
      pChainSet.add(new ProxyChain(next));
    }
    return pChainSet;
  }

  /**
   * @return the proxyChains
   */
  public Set<ProxyChain> getProxyChains() {
    return proxyChains;
  }
}
