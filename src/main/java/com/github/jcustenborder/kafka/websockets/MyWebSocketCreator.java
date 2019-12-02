package com.github.jcustenborder.kafka.websockets;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

public class MyWebSocketCreator implements WebSocketCreator {
  final MyWebSockets webSockets;

  public MyWebSocketCreator(MyWebSockets webSockets) {
    this.webSockets = webSockets;
  }

  @Override
  public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
    return new MyWebSocket(this.webSockets);
  }
}
