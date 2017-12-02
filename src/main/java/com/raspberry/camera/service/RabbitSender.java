package com.raspberry.camera.service;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Serwis służący do wysyłania komunikatów do kloenta przez RabbitMQ
 */
@Component
public class RabbitSender {

    private final RabbitTemplate template;

    private final Queue queue;

    @Autowired
    public RabbitSender(RabbitTemplate template, Queue queue) {
        this.template = template;
        this.queue = queue;
    }

    public void send(String message) {
        this.template.convertAndSend(queue.getName(), message);
    }
}