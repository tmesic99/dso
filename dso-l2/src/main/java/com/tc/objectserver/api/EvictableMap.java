/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package com.tc.objectserver.api;

import com.tc.objectserver.l1.impl.ServerMapEvictionClientObjectReferenceSet;

import java.util.Map;

public interface EvictableMap {

  public int getMaxTotalCount();

  public int getSize();

  public int getTTLSeconds();

  public int getTTISeconds();

  public Map getRandomSamples(int count, ServerMapEvictionClientObjectReferenceSet serverMapEvictionClientObjectRefSet);

  public void evictionCompleted();

  public String getCacheName();

}
