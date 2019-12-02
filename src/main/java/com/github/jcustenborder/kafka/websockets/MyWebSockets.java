package com.github.jcustenborder.kafka.websockets;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyWebSockets {
  private List<MyWebSocket> sockets = new CopyOnWriteArrayList<>();

  public void connect(MyWebSocket socket) {
    this.sockets.add(socket);
  }

  public void close(MyWebSocket socket) {
    this.sockets.remove(socket);
  }

  public void handleRecord(ConsumerRecord<Long, String> record) {
    for(MyWebSocket webSocket: this.sockets) {
      webSocket.send(record);
    }
  }
}
