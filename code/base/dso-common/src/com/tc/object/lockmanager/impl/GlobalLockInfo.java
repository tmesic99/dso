/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tc.object.lockmanager.impl;

import com.tc.io.TCByteBufferInput;
import com.tc.io.TCByteBufferOutput;
import com.tc.io.TCSerializable;
import com.tc.object.locks.LockID;
import com.tc.object.locks.LockIDSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * This class is used to hold the gloabl information of an lock object and passed back to the client when a client
 * queries about the information of a lock.
 */
public class GlobalLockInfo implements TCSerializable {
  private LockID     lockID;
  private int        level;
  private int        lockRequestQueueLength;
  private Collection greedyHoldersInfo;
  private Collection holdersInfo;
  private Collection waitersInfo;

  public GlobalLockInfo() {
    super();
  }

  public GlobalLockInfo(LockID lockID, int level, int lockRequestQueueLength,
                        Collection greedyHolders, Collection holders, Collection waiters) {
    this.lockID = lockID;
    this.level = level;
    this.lockRequestQueueLength = lockRequestQueueLength;
    this.greedyHoldersInfo = greedyHolders;
    this.holdersInfo = holders;
    this.waitersInfo = waiters;
  }

  public int getLockRequestQueueLength() {
    return lockRequestQueueLength;
  }

  public boolean isLocked(int lockLevel) {
    return (this.level == lockLevel)
           && ((holdersInfo != null && holdersInfo.size() > 0) || (greedyHoldersInfo != null && greedyHoldersInfo
               .size() > 0));
  }

  public Collection getGreedyHoldersInfo() {
    return greedyHoldersInfo;
  }

  public Collection getHoldersInfo() {
    return holdersInfo;
  }

  public Collection getWaitersInfo() {
    return waitersInfo;
  }

  public void serializeTo(TCByteBufferOutput serialOutput) {
    new LockIDSerializer(lockID).serializeTo(serialOutput);
    serialOutput.writeInt(level);
    serialOutput.writeInt(lockRequestQueueLength);
    serialOutput.writeInt(holdersInfo.size());
    for (Iterator i = holdersInfo.iterator(); i.hasNext();) {
      GlobalLockStateInfo holderInfo = (GlobalLockStateInfo) i.next();
      holderInfo.serializeTo(serialOutput);
    }
    serialOutput.writeInt(greedyHoldersInfo.size());
    for (Iterator i = greedyHoldersInfo.iterator(); i.hasNext();) {
      GlobalLockStateInfo holderInfo = (GlobalLockStateInfo) i.next();
      holderInfo.serializeTo(serialOutput);
    }
    serialOutput.writeInt(waitersInfo.size());
    for (Iterator i = waitersInfo.iterator(); i.hasNext();) {
      GlobalLockStateInfo holderInfo = (GlobalLockStateInfo) i.next();
      holderInfo.serializeTo(serialOutput);
    }
  }

  public Object deserializeFrom(TCByteBufferInput serialInput) throws IOException {
    LockIDSerializer lidsr = new LockIDSerializer();
    this.lockID = ((LockIDSerializer) lidsr.deserializeFrom(serialInput)).getLockID();
    this.level = serialInput.readInt();
    this.lockRequestQueueLength = serialInput.readInt();
    int size = serialInput.readInt();
    holdersInfo = new ArrayList(size);
    for (int i = 0; i < size; i++) {
      GlobalLockStateInfo holderInfo = new GlobalLockStateInfo();
      holderInfo.deserializeFrom(serialInput);
      holdersInfo.add(holderInfo);
    }
    size = serialInput.readInt();
    greedyHoldersInfo = new ArrayList(size);
    for (int i = 0; i < size; i++) {
      GlobalLockStateInfo holderInfo = new GlobalLockStateInfo();
      holderInfo.deserializeFrom(serialInput);
      greedyHoldersInfo.add(holderInfo);
    }
    size = serialInput.readInt();
    waitersInfo = new ArrayList(size);
    for (int i = 0; i < size; i++) {
      GlobalLockStateInfo holderInfo = new GlobalLockStateInfo();
      holderInfo.deserializeFrom(serialInput);
      waitersInfo.add(holderInfo);
    }
    return this;
  }

  public LockID getLockID() {
    return lockID;
  }
}
