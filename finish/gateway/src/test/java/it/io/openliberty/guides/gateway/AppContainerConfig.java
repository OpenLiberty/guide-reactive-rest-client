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

import org.microshed.testing.SharedContainerConfiguration;
import org.microshed.testing.testcontainers.ApplicationContainer;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;

import io.openliberty.guides.gateway.client.InventoryClient;

public class AppContainerConfig implements SharedContainerConfiguration {

	private static Network network = Network.newNetwork();
	
    @Container
    public static MockServerContainer mockServer = new MockServerContainer()
                    .withNetworkAliases("mock-server")
                    .withNetwork(network);

    public static MockServerClient mockClient;
  
    @Container
    public static ApplicationContainer gateway = new ApplicationContainer()
                    .withAppContextRoot("/")
                    .withReadinessPath("/api/jobs")
                    .withNetwork(network)
                    .withMpRestClient(InventoryClient.class, "http://mock-server:" + MockServerContainer.PORT)
                    .withEnv("GATEWAY_JOB_BASE_URI", "http://mock-server:" + MockServerContainer.PORT);
    
    @Override
    public void startContainers() {
        mockServer.start();
        mockClient = new MockServerClient(
  	          mockServer.getContainerIpAddress(),
  	          mockServer.getServerPort());
        mockClient
            .when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/jobs"))
             .respond(HttpResponse.response()
                .withStatusCode(200)
                .withBody("{ \"results\": [ { \"jobId\": \"my-job-1\", \"result\": 7 }, { \"jobId\": \"my-job-2\", \"result\": 5 } ] } ")
                .withHeader("Content-Type", "application/json"));
        gateway.start();
    }
    
}