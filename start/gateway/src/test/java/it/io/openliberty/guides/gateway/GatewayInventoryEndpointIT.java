// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
package it.io.openliberty.guides.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;


import org.junit.jupiter.api.Test;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jaxrs.RESTClient;
import org.microshed.testing.jupiter.MicroShedTest;



import io.openliberty.guides.gateway.GatewayResource;
import io.openliberty.guides.models.SystemLoad;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;

import org.microshed.testing.kafka.KafkaConsumerClient;
import org.microshed.testing.kafka.KafkaProducerClient;


import io.openliberty.guides.models.SystemLoad.SystemLoadSerializer;


@MicroShedTest
@SharedContainerConfig(AppContainerConfig.class)
public class GatewayInventoryEndpointIT {

    @RESTClient
    public static GatewayResource gatewayResource;

    @KafkaProducerClient(valueSerializer = SystemLoadSerializer.class)
    public static KafkaProducer<String, SystemLoad> producer;

    @KafkaConsumerClient(valueDeserializer = StringDeserializer.class, 
            groupId = "property-name", topics = "requestSystemPropertyTopic", 
            properties = ConsumerConfig.AUTO_OFFSET_RESET_CONFIG + "=earliest")
    public static KafkaConsumer<String, String> propertyConsumer;

    @AfterAll
    public static void cleanup() {
        gatewayResource.resetSystems();
    }

    @Test
    public void testCpuUsage() throws InterruptedException {
        final SystemLoad sl = new SystemLoad("localhost", 1.1);
        producer.send(new ProducerRecord<String, SystemLoad>("systemLoadTopic", sl));
        Thread.sleep(5000);
        final List<SystemLoad> systems = gatewayResource.getSystems();
        Assertions.assertEquals(systems.size(), 1);
        Assertions.assertEquals(sl.hostname, systems.get(0).hostname,
                "Hostname doesn't match!");
        final SystemLoad systemLoad = systems.get(0);
        Assertions.assertEquals(sl.loadAverage, systemLoad.loadAverage,
                "CPU load doesn't match!");
        
    }

    @Test
    public void testGetProperty() {
        int recordsProcessed = 0;
        final ConsumerRecords<String, String> records = propertyConsumer.poll(Duration.ofMillis(3000));
        System.out.println("Polled " + records.count() + " records from Kafka:");
        for (final ConsumerRecord<String, String> record : records) {
            final String p = record.value();
            System.out.println(p);
            assertEquals("os.name", p);
            recordsProcessed++;
        }
        propertyConsumer.commitAsync();
        assertTrue(recordsProcessed > 0, "No records processed");
    }

}