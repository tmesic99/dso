/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tctest;

public class MissingClassApplyTest extends TransparentTestBase {

  private static final int NODE_COUNT = 5;

  public MissingClassApplyTest() {
    disableAllUntil("2010-07-15");
  }

  public void doSetUp(TransparentTestIface t) throws Exception {
    t.getTransparentAppConfig().setClientCount(NODE_COUNT).setApplicationInstancePerClientCount(1).setIntensity(1);
    t.initializeTestRunner();
  }

  protected Class getApplicationClass() {
    return MissingClassApplyTestApp.class;
  }

}
