/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company 2024
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tc.net.protocol.tcm;

import com.tc.async.api.AbstractEventHandler;
import com.tc.async.api.EventContext;
import com.tc.logging.TCLogger;
import com.tc.logging.TCLogging;

public class HydrateHandler extends AbstractEventHandler {
  private static TCLogger logger = TCLogging.getLogger(HydrateHandler.class);

  @Override
  public void handleEvent(EventContext context) {
    HydrateContext hc = (HydrateContext) context;
    TCMessage message = hc.getMessage();

    try {
      message.hydrate();
    } catch (Throwable t) {
      try {
        logger.error("Error hydrating message of type " + message.getMessageType(), t);
      } catch (Throwable t2) {
        // oh well
      }
      message.getChannel().close();
      return;
    }
    hc.getDestSink().add(message);
  }

}
