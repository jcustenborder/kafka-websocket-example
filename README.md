# Introduction

This project creates a web socket server that subscribes to a topic. The example data is for a 
teleconferencing application. The data in the topic is keyed by the conference id. Sending a message 
over the web socket channel allows the caller to subscribe to a feed of what is happening on a particular conference. 

This example creates a single consumer per web server that will consume all of the events for a topic. Since this 
creates a single consumer, history is not provided. Clients are joining at the current offset in the 
topic. 

## Connect 

```bash
websocat ws://127.0.0.1:8080/kafka
```

## Subscribe

This demo only has a few conference ids that do anything. `1234`, `2345`, `3456`


