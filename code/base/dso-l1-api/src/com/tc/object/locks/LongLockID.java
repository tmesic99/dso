/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package com.tc.object.locks;

import com.tc.io.TCByteBufferInput;
import com.tc.io.TCByteBufferOutput;

import java.io.IOException;

public class LongLockID implements LockID {
  /** Indicates no lock identifier */
  public final static LongLockID NULL_ID = new LongLockID(-1);
  private long                   id;

  public LongLockID() {
    // to make TCSerializable happy
    this(-1);
  }

  /**
   * New id
   * 
   * @param id ID value
   */
  public LongLockID(long id) {
    this.id = id;
  }

  /**
   * @return True if is null identifier
   */
  public boolean isNull() {
    return this == NULL_ID;
  }

  /**
   * @return ID type
   */
  public String getIdentifierType() {
    return "LockID";
  }

  public String toString() {
    return getIdentifierType() + "(" + id + ")";
  }

  /**
   * @return String value of id value
   */
  public String asString() {
    return id + "";
  }

  public int hashCode() {
    return (int) id;
  }

  public boolean equals(Object obj) {
    if (obj instanceof LongLockID) {
      LongLockID lid = (LongLockID) obj;
      return this.id == lid.id;
    }
    return false;
  }

  public Object deserializeFrom(TCByteBufferInput serialInput) throws IOException {
    this.id = serialInput.readLong();
    return this;
  }

  public void serializeTo(TCByteBufferOutput serialOutput) {
    serialOutput.writeLong(id);
  }

  public byte getLockType() {
    return LONG_TYPE;
  }
}
