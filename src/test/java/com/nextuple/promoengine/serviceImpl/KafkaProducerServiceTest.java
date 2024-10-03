package com.nextuple.promoengine.serviceImpl;

import static org.mockito.Mockito.*;

import com.nextuple.promoengine.service.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
     void testSendMessage() {

        String topic = "test-topic";
        String message = "Test message";

        kafkaProducerService.sendMessage(topic, message);

        verify(kafkaTemplate, times(1)).send(topic, message);
    }
}
