package com.cinemaabyss.events.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.cinemaabyss.events.model.EventResponse;
import com.cinemaabyss.events.model.MovieEvent;
import com.cinemaabyss.events.model.PaymentEvent;
import com.cinemaabyss.events.model.UserEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EventProducer {

    private static final Logger log = LoggerFactory.getLogger(EventProducer.class);

    private static final String MOVIE_EVENTS_TOPIC = "movie-events";
    private static final String USER_EVENTS_TOPIC = "user-events";
    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public EventResponse sendMovieEvent(MovieEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            SendResult<String, String> result = kafkaTemplate.send(MOVIE_EVENTS_TOPIC, json).get();
            log.info("Sent movie event to partition {} offset {}", result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            return EventResponse.success(
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    event
            );
        } catch (Exception e) {
            log.error("Failed to send movie event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send movie event", e);
        }
    }

    public EventResponse sendUserEvent(UserEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            SendResult<String, String> result = kafkaTemplate.send(USER_EVENTS_TOPIC, json).get();
            log.info("Sent user event to partition {} offset {}", result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            return EventResponse.success(
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    event
            );
        } catch (Exception e) {
            log.error("Failed to send user event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send user event", e);
        }
    }

    public EventResponse sendPaymentEvent(PaymentEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            SendResult<String, String> result = kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, json).get();
            log.info("Sent payment event to partition {} offset {}", result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            return EventResponse.success(
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    event
            );
        } catch (Exception e) {
            log.error("Failed to send payment event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send payment event", e);
        }
    }
}
