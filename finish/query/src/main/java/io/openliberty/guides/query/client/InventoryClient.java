// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2020, 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
// end::copyright[]
package io.openliberty.guides.query.client;

import java.util.List;
import java.util.Properties;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvokerProvider;

import rx.Observable;

@RequestScoped
public class InventoryClient {

    @Inject
    @ConfigProperty(name = "INVENTORY_BASE_URI", defaultValue = "http://localhost:9085")
    private String baseUri;

    public List<String> getSystems() {
        return ClientBuilder.newClient()
                            .target(baseUri)
                            .path("/inventory/systems")
                            .request()
                            .header(HttpHeaders.CONTENT_TYPE,
                                    MediaType.APPLICATION_JSON)
                            .get(new GenericType<List<String>>() { });
    }

    // tag::getSystem[]
    public Observable<Properties> getSystem(String hostname) {
        return ClientBuilder.newClient()
                            .target(baseUri)
                            // tag::register[]
                            .register(RxObservableInvokerProvider.class)
                            // end::register[]
                            .path("/inventory/systems")
                            .path(hostname)
                            .request()
                            .header(HttpHeaders.CONTENT_TYPE,
                                    MediaType.APPLICATION_JSON)
                            // tag::rx[]
                            .rx(RxObservableInvoker.class)
                            // end::rx[]
                            .get(new GenericType<Properties>() { });
    }
    // end::getSystem[]
}
