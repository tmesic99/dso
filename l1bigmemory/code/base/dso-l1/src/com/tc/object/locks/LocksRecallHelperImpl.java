/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package com.tc.object.locks;

import com.tc.async.api.Sink;
import com.tc.async.api.Stage;
import com.tc.object.context.LocksToRecallContext;
import com.tc.object.handler.LockRecaller;

import java.util.Set;

public class LocksRecallHelperImpl implements LocksRecallHelper {

  private final LockRecaller lockRecaller;
  private final Sink         lockRecallSink;

  public LocksRecallHelperImpl(LockRecaller lockRecaller, Stage lockRecallStage) {
    this.lockRecaller = lockRecaller;
    this.lockRecallSink = lockRecallStage.getSink();
  }

  public void initiateLockRecall(Set<LockID> lockIds) {
    this.lockRecallSink.add(new LocksToRecallContext(lockIds));
  }

  public void recallLocksInline(Set<LockID> lockIds) {
    lockRecaller.recallLocksInline(lockIds);
  }

}
