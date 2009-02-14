/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tctest.server.appserver.unit;

import com.tc.test.server.appserver.deployment.ServerTestSetup;

import junit.framework.Test;

public class SessionTimeoutWithoutSLTest extends SessionTimeoutTest {

  public static Test suite() {
    return new ServerTestSetup(SessionTimeoutWithoutSLTest.class);
  }

  @Override
  public boolean isSessionLockingTrue() {
    return false;
  }

}
