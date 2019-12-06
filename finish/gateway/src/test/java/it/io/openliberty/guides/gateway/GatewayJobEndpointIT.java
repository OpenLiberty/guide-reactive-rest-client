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

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jupiter.MicroShedTest;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import io.openliberty.guides.gateway.GatewayJobResource;
import io.openliberty.guides.models.Job;
import io.openliberty.guides.models.JobList;
import it.io.openliberty.guides.gateway.AppContainerConfig;

@MicroShedTest
@SharedContainerConfig(AppContainerConfig.class)
public class GatewayJobEndpointIT {

    @Inject
    public static GatewayJobResource jobResource;
    
    @BeforeAll
    public static void setup() throws InterruptedException {
    	
    	AppContainerConfig.mockClient
            .when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/jobs"))
            .respond(HttpResponse.response()
                .withStatusCode(200)
                .withBody("{ \"results\": [ { \"jobId\": \"my-job-1\", \"result\": 7 }, { \"jobId\": \"my-job-2\", \"result\": 5 } ] } ")
                .withHeader("Content-Type", "application/json"));

    	AppContainerConfig.mockClient
            .when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/jobs"))
            .respond(HttpResponse.response()
                .withStatusCode(200)
                .withBody("{ \"jobId\": \"my-job-id\" }")
                .withHeader("Content-Type", "application/json"));
    }
    
    // tag::testCreateJob[]
    @Test
    public void testCreateJob() throws InterruptedException {
    	Job j = jobResource.createJob();
    	assertEquals("my-job-id", j.getJobId(), "Failed to create job.");
    }
    // end::testCreateJob[]


    // tag::testGetJobs[]
    @Test
    public void testGetJobs() throws InterruptedException {
    	JobList jobs = jobResource.getJobs();
    	assertEquals(2,jobs.getCount());
    	assertEquals(6.0, jobs.getAverageResult().getAsDouble(), 0.01);
    	assertEquals(2,jobs.getResults().size());
    }
    // end::testGetJobs[]

}
