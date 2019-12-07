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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jaxrs.RESTClient;
import org.microshed.testing.jupiter.MicroShedTest;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import io.openliberty.guides.gateway.GatewayInventoryResource;
import io.openliberty.guides.models.InventoryList;
import io.openliberty.guides.models.SystemData;

@MicroShedTest
@SharedContainerConfig(AppContainerConfig.class)
public class GatewayInventoryEndpointIT {

    @RESTClient
    public static GatewayInventoryResource inventoryResource;
    
    @BeforeAll
    public static void setup() throws InterruptedException {
    	AppContainerConfig.mockClient
            .when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/inventory/systems"))
            .respond(HttpResponse.response()
                .withStatusCode(200)
                .withBody("{ \"systems\": [ { \"hostname\": \"banana\", \"properties\": { \"java.vendor\": \"you\", \"system.busy\": \"false\" } } ], \"total\": 1 }")
                .withHeader("Content-Type", "application/json"));

    	AppContainerConfig.mockClient
            .when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/inventory/systems/coconut"))
            .respond(HttpResponse.response()
                .withStatusCode(200)
                .withBody("{ \"hostname\": \"coconut\", \"properties\": { \"java.vendor\": \"me\" } }")
                .withHeader("Content-Type", "application/json"));
    }
    
    @Test
    public void testAddSystem() {
    	SystemData s = inventoryResource.getSystem("coconut");
        assertEquals("coconut", s.getHostname());
    }

    @Test
    public void testGetSystems() {
    	InventoryList systems = inventoryResource.getSystems();
        assertEquals(1, systems.getTotal());
        assertEquals(1, systems.getSystems().size());
    }

}
