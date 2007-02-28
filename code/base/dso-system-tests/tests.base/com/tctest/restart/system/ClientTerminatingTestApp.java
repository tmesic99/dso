/*
 * All content copyright (c) 2003-2006 Terracotta, Inc., except as may otherwise be noted in a separate copyright notice.  All rights reserved.
 */
package com.tctest.restart.system;

import org.apache.commons.io.FileUtils;

import com.tc.objectserver.control.ExtraL1ProcessControl;
import com.tc.simulator.app.ApplicationConfig;
import com.tc.simulator.listener.ListenerProvider;
import com.tctest.ServerCrashingAppBase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClientTerminatingTestApp extends ServerCrashingAppBase {

  private static final int      LOOP_COUNT = 2;
  private static final List     queue      = new ArrayList();

  private int                   id         = -1;
  private long                  count      = 0;
  private ExtraL1ProcessControl client;

  public ClientTerminatingTestApp(String appId, ApplicationConfig cfg, ListenerProvider listenerProvider) {
    super(appId, cfg, listenerProvider);
  }

  public void run() {
    List myList = new ArrayList();
    synchronized (queue) {
      if (id != -1) { throw new AssertionError("Only one controller per Instance allowed. Check the Execution count"); }
      id = queue.size();
      queue.add(myList);
    }

    Random random = new Random();
    int times = LOOP_COUNT;
    try {
      while (times-- >= 0) {
        long toAdd = random.nextInt(10) * 10L + 1;
        File workingDir = new File(getConfigFileDirectoryPath(), "client-" + id + "-" + times);
        FileUtils.forceMkdir(workingDir);
        System.err.println(this + "Creating Client with args " + id + " , " + toAdd);
        client = new ExtraL1ProcessControl(getHostName(), getPort(), Client.class, getConfigFilePath(), new String[] {
            "" + id, "" + toAdd }, workingDir);
        client.start(20000);
        int exitCode = client.waitFor();
        if (exitCode == 0) {
          System.err.println(this + "Client existed Normally");
          verify(myList, toAdd);
        } else {
          String errorMsg = this + "Client existed Abnormally. Exit code = " + exitCode;
          System.err.println(errorMsg);
          throw new AssertionError(errorMsg);
        }
      }
    } catch (Exception e) {
      System.err.println(this + " Got - " + e);
      throw new AssertionError(e);
    }

  }

  private void verify(List myList, long toAdd) {
    synchronized (myList) {
      if (toAdd != myList.size()) {
        String errorMsg = this + " Expected " + toAdd + " elements in the list. But Found " + myList.size();
        System.err.println(errorMsg);
        throw new AssertionError(errorMsg);
      }
    }
    for (int i = 0; i < myList.size(); i++) {
      synchronized (myList) {
        if ((++count != ((Long) myList.get(i)).longValue())) {
          String errorMsg = this + " Expected " + count + " value in the list. But Found " + myList.get(i);
          System.err.println(errorMsg);
          throw new AssertionError(errorMsg);
        }
      }
    }
  }

  public String toString() {
    return "Controller(" + id + ") :";
  }

  public static class Client {
    private int  id;
    private long addCount;

    public Client(int i, long addCount) {
      this.id = i;
      this.addCount = addCount;
    }

    public static void main(String args[]) {
      if (args.length != 2) { throw new AssertionError("Usage : Client <id> <num of increments>"); }

      Client client = new Client(Integer.parseInt(args[0]), Long.parseLong(args[1]));
      client.execute();
    }

    // Writen so that many transactions are created ...
    public void execute() {
      List myList = null;
      long count = 0;
      System.err.println(this + " execute : addCount = " + addCount);
      synchronized (queue) {
        myList = (List) queue.get(id);
      }
      synchronized (myList) {
        if (myList.size() > 0) {
          count = ((Long) myList.get(myList.size() - 1)).longValue();
          myList.clear();
        }
      }
      while (addCount-- > 0) {
        synchronized (myList) {
          myList.add(new Long(++count));
        }
      }
      System.err.println(this + " put till :" + count);
      System.exit(0);
    }

    public String toString() {
      return "Client(" + id + ") :";
    }
  }

}
