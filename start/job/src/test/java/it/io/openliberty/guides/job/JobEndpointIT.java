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
package it.io.openliberty.guides.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jupiter.MicroShedTest;

import io.openliberty.guides.job.JobResource;
import io.openliberty.guides.models.Job;

@MicroShedTest
@SharedContainerConfig(AppContainerConfig.class)
public class JobEndpointIT {

    private final static int RETRIES = 5;
    private final static int BACKOFF_MULTIPLIER = 2;
    
    private static KafkaProducer<String, String> producer;

    @Inject
    public static JobResource jobResource;

    @BeforeAll
    public static void setup() throws InterruptedException {
    	String KAFKA_SERVER = AppContainerConfig.kafka.getBootstrapServers();
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(properties);
    }

    @Test
    public void testCreateJob() {
    	Job j = jobResource.createJob();
        String jobId = j.getJobId(); 
        assertTrue(jobId.matches("^\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}$"), "jobId not returned from service");
    }

    @Test
    public void testJobNotExists() {
        Response response = jobResource.getJobResult("my-job-id");
        assertEquals(404, response.getStatus());
    }

	@Test
    public void testConsumeJob() throws InterruptedException {
        producer.send(new ProducerRecord<String,String>("job-result-topic", "{ \"jobId\": \"my-produced-job-id\", \"result\": 7 }"));
        Response response = jobResource.getJobResult("my-produced-job-id");
        int backoff = 500;
        for (int i = 0; i < RETRIES && response.getStatus() != 200; i++) {
        	response = jobResource.getJobResult("my-produced-job-id");
            Thread.sleep(backoff);
            backoff *= BACKOFF_MULTIPLIER;
        }

        assertEquals(200, response.getStatus());

        JsonObject obj = response.readEntity(JsonObject.class);
        assertEquals(7, obj.getInt("result"));
    }

}
