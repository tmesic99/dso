/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package com.tc.object.metadata;

import com.tc.io.TCByteBufferInput;
import com.tc.io.TCByteBufferOutput;
import com.tc.object.metadata.NVPair.BooleanNVPair;
import com.tc.object.metadata.NVPair.ByteArrayNVPair;
import com.tc.object.metadata.NVPair.ByteNVPair;
import com.tc.object.metadata.NVPair.CharNVPair;
import com.tc.object.metadata.NVPair.DateNVPair;
import com.tc.object.metadata.NVPair.DoubleNVPair;
import com.tc.object.metadata.NVPair.FloatNVPair;
import com.tc.object.metadata.NVPair.IntNVPair;
import com.tc.object.metadata.NVPair.LongNVPair;
import com.tc.object.metadata.NVPair.ShortNVPair;
import com.tc.object.metadata.NVPair.StringNVPair;

import java.io.IOException;
import java.util.Date;

public enum ValueType {
  BOOLEAN {
    @Override
    public NVPair deserializeFrom(String name, TCByteBufferInput in) throws IOException {
      return new BooleanNVPair(name, in.readBoolean());
    }

    @Override
    void serializeTo(NVPair nvPair, TCByteBufferOutput out) {
      out.writeBoolean(((BooleanNVPair) nvPair).getValue());
    }
  },

  BYTE {
    @Override
    NVPair deserializeFrom(String name, TCByteBufferInput in) throws IOException {
      return new ByteNVPair(name, in.readByte());
    }

    @Override
    void serializeTo(NVPair nvPair, TCByteBufferOutput out) {
      out.writeByte(((ByteNVPair) nvPair).getValue());
    }
  },

  CHAR {
    @Override
    NVPair deserializeFrom(String name, TCByteBufferInput in) throws IOException {
      return new CharNVPair(name, in.readChar());
    }

    @Override
    void serializeTo(NVPair nvPair, TCByteBufferOutput out) {
      out.writeChar(((CharNVPair) nvPair).getValue());
    }
  },

  DOUBLE {
    @Override
    NVPair deserializeFrom(String name, TCByteBufferInput in) throws IOException {
      return new DoubleNVPair(name, in.readDouble());
    }

    @Override
    void serializeTo(NVPair nvPair, TCByteBufferOutput out) {
      out.writeDouble(((DoubleNVPair) nvPair).getValue());
    }
  },

  FLOAT {
    @Override
    NVPair deserializeFrom(String name, TCByteBufferInput in) throws IOException {
      return new FloatNVPair(name, in.readFloat());
    }

    @Override
    void serializeTo(NVPair nvPair, TCByteBufferOutput out) {
      out.writeFloat(((FloatNVPair) nvPair).getValue());
    }
  },

  INT {
    @Override
    NVPair deserializeFrom(String name, TCByteBufferInput in) throws IOException {
      return new IntNVPair(name, in.readInt());
    }

    @Override
    void serializeTo(NVPair nvPair, TCByteBufferOutput out) {
      out.writeInt(((IntNVPair) nvPair).getValue());
    }
  },

  SHORT {
    @Override
    NVPair deserializeFrom(String name, TCByteBufferInput in) throws IOException {
      return new ShortNVPair(name, in.readShort());
    }

    @Override
    void serializeTo(NVPair nvPair, TCByteBufferOutput out) {
      out.writeShort(((ShortNVPair) nvPair).getValue());
    }
  },

  LONG {
    @Override
    NVPair deserializeFrom(String name, TCByteBufferInput in) throws IOException {
      return new LongNVPair(name, in.readLong());
    }

    @Override
    void serializeTo(NVPair nvPair, TCByteBufferOutput out) {
      out.writeLong(((LongNVPair) nvPair).getValue());
    }
  },

  STRING {
    @Override
    NVPair deserializeFrom(String name, TCByteBufferInput in) throws IOException {
      return new StringNVPair(name, in.readString());
    }

    @Override
    void serializeTo(NVPair nvPair, TCByteBufferOutput out) {
      out.writeString(((StringNVPair) nvPair).getValue());
    }
  },

  DATE {
    @Override
    NVPair deserializeFrom(String name, TCByteBufferInput in) throws IOException {
      return new DateNVPair(name, new Date(in.readLong()));
    }

    @Override
    void serializeTo(NVPair nvPair, TCByteBufferOutput out) {
      out.writeLong(((DateNVPair) nvPair).getValue().getTime());
    }
  },

  BYTE_ARRAY {
    @Override
    NVPair deserializeFrom(String name, TCByteBufferInput in) throws IOException {
      int len = in.readInt();
      byte[] data = new byte[len];
      in.read(data, 0, len);
      return new ByteArrayNVPair(name, data);
    }

    @Override
    void serializeTo(NVPair nvPair, TCByteBufferOutput out) {
      out.write(((ByteArrayNVPair) nvPair).getValue());
    }
  };

  static {
    int length = ValueType.values().length;
    if (length > 127) {
      // The encoding logic could support all 256 values in the encoded byte or we could expand to 2 bytes if needed
      throw new AssertionError("Current implementation does not allow for more 127 types");
    }
  }

  NVPair deserializeFrom(TCByteBufferInput in) throws IOException {
    String name = in.readString();
    return deserializeFrom(name, in);
  }

  abstract NVPair deserializeFrom(String name, TCByteBufferInput in) throws IOException;

  abstract void serializeTo(NVPair nvPair, TCByteBufferOutput out);

}
