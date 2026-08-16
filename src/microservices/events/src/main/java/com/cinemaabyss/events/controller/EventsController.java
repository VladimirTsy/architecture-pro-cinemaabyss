package com.cinemaabyss.events.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinemaabyss.events.model.EventResponse;
import com.cinemaabyss.events.model.MovieEvent;
import com.cinemaabyss.events.model.PaymentEvent;
import com.cinemaabyss.events.model.UserEvent;
import com.cinemaabyss.events.producer.EventProducer;

@RestController
@RequestMapping("/api/events")
public class EventsController {

    private final EventProducer eventProducer;

    public EventsController(EventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    @GetMapping("/health")
    public ResponseEntity<Boolean> health() {
        return ResponseEntity.ok(true);
    }

    @PostMapping("/movie")
    public ResponseEntity<EventResponse> sendMovieEvent(@RequestBody MovieEvent event) {
        EventResponse response = eventProducer.sendMovieEvent(event);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/user")
    public ResponseEntity<EventResponse> sendUserEvent(@RequestBody UserEvent event) {
        EventResponse response = eventProducer.sendUserEvent(event);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/payment")
    public ResponseEntity<EventResponse> sendPaymentEvent(@RequestBody PaymentEvent event) {
        EventResponse response = eventProducer.sendPaymentEvent(event);
        return ResponseEntity.status(201).body(response);
    }
}
