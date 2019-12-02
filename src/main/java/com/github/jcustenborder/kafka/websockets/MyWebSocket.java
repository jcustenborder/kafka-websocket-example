package com.github.jcustenborder.kafka.websockets;

import com.google.common.primitives.Longs;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class MyWebSocket {
  private static final Logger log = LoggerFactory.getLogger(MyWebSocket.class);
  private Set<Long> subscriptions = new HashSet<>();
  private Session session;
  private final MyWebSockets myWebSockets;

  public MyWebSocket(MyWebSockets myWebSockets) {
    this.myWebSockets = myWebSockets;
  }

  @OnWebSocketConnect
  public void connect(Session session) {
    this.session = session;
    if (session.isOpen()) {
      log.info("webSocketConnect() - {}", session.getRemoteAddress());
      this.myWebSockets.connect(this);
      session.getRemote().sendStringByFuture("Send a number to subscribe to a conference");
    }
  }

  @OnWebSocketMessage
  public void receiveText(Session session, String message) {
    log.info("receiveText() - session = {} message = {}", session.getRemoteAddress(), message);

    Long subscription = Longs.tryParse(message.trim());

    if (null != subscription) {
      this.subscriptions.add(subscription);
    }
  }

  @OnWebSocketClose
  public void close(int statusCode, String reason) {
    log.info("close() - statusCode = {} reason = {}", statusCode, reason);
    this.myWebSockets.close(this);
    this.session = null;
  }

  public void send(ConsumerRecord<Long, String> record) {
    if (!subscriptions.contains(record.key())) {
      return;
    }

    if (session.isOpen()) {
      session.getRemote().sendStringByFuture(record.value());
    }
  }
}
