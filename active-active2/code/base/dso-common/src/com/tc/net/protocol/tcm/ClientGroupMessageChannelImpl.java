/*
 * All content copyright (c) 2003-2007 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tc.net.protocol.tcm;

import com.tc.logging.TCLogger;
import com.tc.logging.TCLogging;
import com.tc.net.MaxConnectionsExceededException;
import com.tc.net.core.ConnectionAddressProvider;
import com.tc.net.groups.ClientID;
import com.tc.net.groups.GroupID;
import com.tc.net.groups.NodeID;
import com.tc.net.groups.NodeIDImpl;
import com.tc.net.protocol.NetworkStackID;
import com.tc.net.protocol.TCNetworkMessage;
import com.tc.net.protocol.transport.MessageTransport;
import com.tc.net.protocol.transport.TransportHandshakeMessage;
import com.tc.object.session.SessionProvider;
import com.tc.util.TCTimeoutException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;

public class ClientGroupMessageChannelImpl extends ClientMessageChannelImpl implements
    ClientGroupMessageChannel {
  private static final TCLogger  logger = TCLogging.getLogger(ClientGroupMessageChannel.class);
  private final TCMessageFactory msgFactory;
  private final SessionProvider  sessionProvider;

  private CommunicationsManager  communicationsManager;
  private ClientMessageChannel[] channels;
  private GroupID[]              servers;

  public ClientGroupMessageChannelImpl(TCMessageFactory msgFactory, SessionProvider sessionProvider,
                                           final int maxReconnectTries, CommunicationsManager communicationsManager,
                                           ConnectionAddressProvider[] addressProviders) {
    super(msgFactory, null, sessionProvider, null, null, false);
    this.msgFactory = msgFactory;
    this.sessionProvider = sessionProvider;

    this.communicationsManager = communicationsManager;
    this.channels = new ClientMessageChannel[addressProviders.length];
    this.servers = new GroupID[addressProviders.length];

    logger.info("Create active channels");
    for (int i = 0; i < addressProviders.length; ++i) {
      boolean isActiveCoordinator = (i == 0);
      channels[i] = this.communicationsManager.createClientChannel(this.sessionProvider, -1, null, 0, 10000,
                                                                   addressProviders[i], 
                                                                   TransportHandshakeMessage.NO_CALLBACK_PORT, null,
                                                                   this.msgFactory,
                                                                   new TCMessageRouterImpl(), this, isActiveCoordinator);
      servers[i] = (GroupID)channels[i].getServerID();
      logger.info("Created sub-channel" + i + ":" + addressProviders[i]);
    }
    setClientID(ClientID.NULL_ID);
    setServerID(GroupID.NULL_ID);
  }

  public ClientMessageChannel getActiveCoordinator() {
    return channels[0];
  }

  public ChannelID getActiveActiveChannelID() {
    return getActiveCoordinator().getChannelID();
  }

  public NodeID makeNodeMultiplexId(ChannelID cid, ConnectionAddressProvider addressProvider) {
    // XXX ....
    return (new NodeIDImpl(addressProvider + cid.toString(), addressProvider.toString().getBytes()));
  }

  public ClientMessageChannel[] getChannels() {
    return (channels);
  }

  public NodeID[] getMultiplexIDs() {
    return (servers);
  }

  public ClientMessageChannel getChannel(NodeID id) {
    for (int i = 0; i < servers.length; ++i) {
      if (id.equals(servers[i])) { return (channels[i]); }
    }
    return null;
  }

  public TCMessage createBroadcastMessage(TCMessageType type) {
    TCMessage rv = msgFactory.createMessage(this, type);
    return rv;
  }

  public TCMessage createMessage(NodeID id, TCMessageType type) {
    TCMessage rv = msgFactory.createMessage(getChannel(id), type);
    return rv;
  }

  public TCMessage createMessage(TCMessageType type) {
    TCMessage rv = msgFactory.createMessage(getChannels()[0], type);
    return rv;
  }

  public NetworkStackID open() throws TCTimeoutException, UnknownHostException, IOException,
      MaxConnectionsExceededException {
    NetworkStackID nid = null;
    for (int i = 0; i < channels.length; ++i) {
      try {
        nid = channels[i].open();
      } catch (TCTimeoutException e) {
        throw new TCTimeoutException(channels[i].getConnectionAddress().toString() + " " + e);
      } catch (UnknownHostException e) {
        throw new UnknownHostException(channels[i].getConnectionAddress().toString() + " " + e);
      } catch (MaxConnectionsExceededException e) {
        throw new MaxConnectionsExceededException(channels[i].getConnectionAddress().toString() + " " + e);
      }
      logger.info("Opened sub-channel: "+ channels[i].getConnectionAddress().toString());
    }
    logger.info("all active sub-channels opened");
    setClientID(new ClientID(getChannelID()));
    return nid;
  }

  public ChannelID getChannelID() {
    // return one of active-coordinator, they are same for all channels
    return getActiveCoordinator().getChannelID();
  }

  public int getConnectCount() {
    // an aggregate of all channels
    int count = 0;
    for (int i = 0; i < channels.length; ++i)
      count += channels[i].getConnectCount();
    return count;
  }

  public int getConnectAttemptCount() {
    // an aggregate of all channels
    int count = 0;
    for (int i = 0; i < channels.length; ++i)
      count += channels[i].getConnectAttemptCount();
    return count;
  }

  public void routeMessageType(TCMessageType messageType, TCMessageSink dest) {
    for (int i = 0; i < channels.length; ++i)
      channels[i].routeMessageType(messageType, dest);
  }

  /*
   * send broadcast message
   */
  public void send(final TCNetworkMessage message) {
    message.setSendCount(channels.length - 1);
    for (int i = 0; i < channels.length; ++i)
      channels[i].send(message);
  }

  public void notifyTransportConnected(MessageTransport transport) {
    throw new AssertionError();
  }

  public void notifyTransportDisconnected(MessageTransport transport) {
    throw new AssertionError();
  }

  public void notifyTransportConnectAttempt(MessageTransport transport) {
    throw new AssertionError();
  }

  public void notifyTransportClosed(MessageTransport transport) {
    throw new AssertionError();
  }

  public ChannelIDProvider getChannelIDProvider() {
    // return one from active-coordinator
    return getActiveCoordinator().getChannelIDProvider();
  }

  public void close() {
    for (int i = 0; i < channels.length; ++i)
      channels[i].close();
  }

  public boolean isConnected() {
    if (channels.length == 0) return false;
    for (int i = 0; i < channels.length; ++i) {
      if (!channels[i].isConnected()) return false;
    }
    return true;
  }

  public boolean isOpen() {
    if (channels.length == 0) return false;
    for (int i = 0; i < channels.length; ++i) {
      if (!channels[i].isOpen()) return false;
    }
    return true;
  }

  public ClientMessageChannel channel() {
    // return the active-coordinator
    return getActiveCoordinator();
  }
  
  public ConnectionAddressProvider getConnectionAddress() {
    return getActiveCoordinator().getConnectionAddress();
  }

  /*
   * As a middleman between ClientHandshakeManager and multiple ClientMessageChannels. Bookkeeping sub-channels' events
   * Notify connected only when all channel connected. Notify disconnected when any channel disconnected Notify closed
   * when any channel closed
   */
  private class ClientGroupMessageChannelEventListener implements ChannelEventListener {
    private final ChannelEventListener listener;
    private HashSet                    connectedSet = new HashSet();
    private final ClientMessageChannel channel;

    public ClientGroupMessageChannelEventListener(ChannelEventListener listener, ClientMessageChannel channel) {
      this.listener = listener;
      this.channel = channel;
    }

    public void notifyChannelEvent(ChannelEvent event) {
      if (event.getType() == ChannelEventType.TRANSPORT_DISCONNECTED_EVENT) {
        if (connectedSet.remove(event.getChannel())) {
          fireEvent(event);
        }
      } else if (event.getType() == ChannelEventType.TRANSPORT_CONNECTED_EVENT) {
        connectedSet.add(event.getChannel());
        if (connectedSet.size() == channels.length) {
          fireEvent(event);
        }
      } else if (event.getType() == ChannelEventType.CHANNEL_CLOSED_EVENT) {
        if (connectedSet.remove(event.getChannel())) {
          fireEvent(event);
        }
      }
    }

    private void fireEvent(ChannelEvent event) {
      listener.notifyChannelEvent(new ChannelEventImpl(event.getType(), channel));
    }
  }

  public void addListener(ChannelEventListener listener) {
    ClientGroupMessageChannelEventListener middleman = new ClientGroupMessageChannelEventListener(listener, this);
    for (int i = 0; i < channels.length; ++i)
      channels[i].addListener(middleman);
  }

}