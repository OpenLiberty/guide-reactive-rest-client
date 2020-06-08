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

import org.bouncycastle.crypto.CryptoServicesRegistrar.Property;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jaxrs.RESTClient;
import org.microshed.testing.jupiter.MicroShedTest;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import io.openliberty.guides.gateway.GatewayResource;
import io.openliberty.guides.models.SystemLoad;
import io.openliberty.guides.models.PropertyMessage;

@MicroShedTest
@SharedContainerConfig(AppContainerConfig.class)
public class GatewayInventoryEndpointIT {

    @RESTClient
    public static GatewayResource inventoryResource;
    
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
    public void testAddSystem() throws InterruptedException {
    	SystemLoad s = inventoryResource.getSystem("coconut");
        assertEquals("coconut", s.hostname);
    }

    @Test
    public void testGetSystems() throws InterruptedException {
        List<SystemLoad> systems = inventoryResource.getSystems();
        assertEquals(1, systems.size());
    }

}