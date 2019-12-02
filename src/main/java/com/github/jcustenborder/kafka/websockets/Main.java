package com.github.jcustenborder.kafka.websockets;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
  private static final Logger log = LoggerFactory.getLogger(Main.class);

  static List<Long> CONFERENCE_IDS = ImmutableList.of(1234L, 2345L, 3456L);
  static List<String> NAMES = ImmutableList.of("Fred", "Mary", "Susan", "UJ", "Jeremy", "Cheryl", "Kylie", "Dani", "Mitch", "Brian");
  static List<String> ACTIONS = ImmutableList.of("Talking", "Muted", "Sharing", "Messaging", "Connecting", "Disconnecting");

  static Random random = new Random();

  static <T> T random(List<T> list) {
    int index = random.nextInt(list.size());
    return list.get(index);
  }

  public static void main(String... args) throws Exception {

    Server server = new Server(8080);
    ServletHandler servletHandler = new ServletHandler();
    server.setHandler(servletHandler);
    MyWebSockets webSockets = new MyWebSockets();
    MyWebSocketCreator webSocketCreator = new MyWebSocketCreator(webSockets);
    MyWebSocketServlet myWebSocketServlet = new MyWebSocketServlet(webSocketCreator);
    ServletHolder servletHolder = new ServletHolder(myWebSocketServlet);
    servletHandler.addServletWithMapping(servletHolder, "/*");
    server.start();


    Properties kafkaSettings = new Properties();
    kafkaSettings.setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");


    Future<?> consumerThread = Executors.newSingleThreadExecutor().submit((Runnable) () -> {
      Properties consumerSettings = new Properties();
      consumerSettings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
      consumerSettings.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
      consumerSettings.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      consumerSettings.put(ConsumerConfig.GROUP_ID_CONFIG, String.format("websockets-%s", UUID.randomUUID().toString()));
      consumerSettings.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
      consumerSettings.putAll(kafkaSettings);
      try (Consumer<Long, String> consumer = new KafkaConsumer<>(consumerSettings)) {
        consumer.subscribe(Arrays.asList("events"));
        while (true) {
          ConsumerRecords<Long, String> records = consumer.poll(Duration.ofSeconds(1));
          for (ConsumerRecord<Long, String> record : records) {
            webSockets.handleRecord(record);
          }
        }
      } catch (Exception ex) {
        log.error("Exception thrown", ex);
      }
    });


    Future<?> producerThread = Executors.newSingleThreadExecutor().submit((Runnable) () -> {
      Properties producerSettings = new Properties();
      producerSettings.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
      producerSettings.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
      producerSettings.putAll(kafkaSettings);
      RateLimiter limiter = RateLimiter.create(10D);
      try (Producer<Long, String> producer = new KafkaProducer<>(producerSettings)) {
        while (true) {
          limiter.acquire();
          Long conference = random(CONFERENCE_IDS);
          String value = String.format("%s is %s", random(NAMES), random(ACTIONS));
          ProducerRecord<Long, String> record = new ProducerRecord<>(
              "events",
              conference,
              value
          );
          producer.send(record);
        }
      } catch (Exception ex) {
        log.error("Exception thrown", ex);
      }
    });


    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      consumerThread.cancel(true);
      try {
        server.stop();
      } catch (Exception e) {
        log.error("Exception while shutting down jetty", e);
      }

    }));


    server.join();


  }


}
