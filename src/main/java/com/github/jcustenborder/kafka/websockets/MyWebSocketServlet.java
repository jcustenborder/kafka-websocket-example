package com.github.jcustenborder.kafka.websockets;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet(name = "MyEcho WebSocket Servlet", urlPatterns = {"/kafka"})
public class MyWebSocketServlet extends WebSocketServlet {
  final MyWebSocketCreator webSocketCreator;

  public MyWebSocketServlet(MyWebSocketCreator webSocketCreator) {
    this.webSocketCreator = webSocketCreator;
  }

  @Override
  public void configure(WebSocketServletFactory factory) {
    factory.setCreator(this.webSocketCreator);
  }
}
