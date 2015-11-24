package com.izettle.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaEventProducer<T> implements AutoCloseable{
    private final String topic;
    private final ObjectMapper objectMapper;
    private KafkaProducer<String, String> producer;

    public KafkaEventProducer(
        String topic,
        String kafkaHost
    ) {
        this(topic, kafkaHost, Optional.empty());
    }

    public KafkaEventProducer(String topic, String kafkaHost, ObjectMapper objectMapper) {
        this(topic, kafkaHost, Optional.ofNullable(objectMapper));
    }
    private KafkaEventProducer(String topic, String kafkaHost, Optional<ObjectMapper> objectMapper) {
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost); //"localhost:9092"
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        this.producer = new KafkaProducer<>(producerProps);
        this.objectMapper = objectMapper.orElse(new ObjectMapper());
        this.topic = topic;
    }

    public void send(T dto) throws JsonProcessingException {
        send(dto, this.topic);
    }

    public void send(T dto, String topic) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(dto);
        producer.send(new ProducerRecord<>(topic, json));
    }

    public void sendBatch(Collection<T> batch) throws JsonProcessingException {
        sendBatch(batch, this.topic);
    }

    public void sendBatch(Collection<T> batch, String topic) throws JsonProcessingException {
        for (T msg : batch) {
            producer.send(new ProducerRecord<>(topic, objectMapper.writeValueAsString(msg)));
        }
    }



    @Override
    public void close() throws Exception {
        this.producer.close(30, TimeUnit.SECONDS);

    }

}
