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
package it.io.openliberty.guides.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jupiter.MicroShedTest;

import io.openliberty.guides.inventory.InventoryResource;
import io.openliberty.guides.models.InventoryList;
import io.openliberty.guides.models.SystemData;
import it.io.openliberty.guides.inventory.AppContainerConfig;

@MicroShedTest
@SharedContainerConfig(AppContainerConfig.class)
public class InventoryEndpointIT {

    private final int RETRIES = 8;
    private final int BACKOFF_MULTIPLIER = 2;
    private final int BASE_BACKOFF = 500;

    @Inject
    public static InventoryResource inventoryResource;
    
    private KafkaProducer<String, String> producer;

    @BeforeEach
    public void setup() throws InterruptedException {
        String KAFKA_SERVER = AppContainerConfig.kafka.getBootstrapServers();
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        this.producer = new KafkaProducer<>(properties);
    }

    @Test
    @Order(1)
    public void testReset() {
    	inventoryResource.reset();
    	InventoryList list = inventoryResource.listContents();
        assertEquals(0, list.getTotal());
    }
    
    @Test
    @Order(2)
    public void testConsumeSystem() throws InterruptedException, IOException {

        // Add a system to the inventory via kafka
        String props = getResource("props.json");
        producer.send(new ProducerRecord<String,String>("system-topic", props));

        // wait until total is greater than 0
        InventoryList list = inventoryResource.listContents();
        int total = list.getTotal();
        int backoff = BASE_BACKOFF;
        for (int i = 0; i < RETRIES && total == 0; i++) {
            Thread.sleep(backoff);
            backoff *= BACKOFF_MULTIPLIER;
            list = inventoryResource.listContents();
            total = list.getTotal();
        }
        assertTrue(total > 0, String.format("Total (%s) is not greater than 0", total));

        // Make system busy
        String busyProps = getResource("props.busy.json");
        producer.send(new ProducerRecord<String, String>("system-topic", busyProps));

        // wait until the system is busy
        list = inventoryResource.listContents();
        List<SystemData> systems = list.getSystems();
        backoff = BASE_BACKOFF;
        for (int i = 0; i < RETRIES && !getPropertyFromSystems("myhost", "system.busy", systems).equals("true"); i++) {
            Thread.sleep(backoff);
            backoff *= BACKOFF_MULTIPLIER;
            list = inventoryResource.listContents();
            systems = list.getSystems();
        }
        assertEquals("true", getPropertyFromSystems("myhost", "system.busy", systems));
    }

    private String getResource(String filename) throws IOException {
        ClassLoader loader = getClass().getClassLoader();
        File file = new File(loader.getResource(filename).getFile());
        BufferedReader reader = new BufferedReader(new FileReader(file));

        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }

        reader.close();
        return builder.toString();
    }
  
    private String getPropertyFromSystems(String hostname, String property, List<SystemData> systems) {
        for (SystemData s : systems) {
            if (s.getHostname().equals(hostname)) {
                String result = s.getProperties().getProperty(property);
                if (result != null) return result;
            }
        }

        return "";
    }

}
