// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
package io.openliberty.guides.gateway.client;

import io.openliberty.guides.models.*;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvokerProvider;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;

import rx.Observable;

@RequestScoped
public class InventoryClient {

    @Inject
    @ConfigProperty(name = "GATEWAY_BASE_URI", defaultValue = "http://localhost:9090")
    private String baseUri;

    @Inject
    @ConfigProperty(name = "INVENTORY_BASE_URI", defaultValue = "http://localhost:9085")
    private String inventory_baseUri;

    private WebTarget target;

    public InventoryClient() {
        this.target = null;
    }

    public Response getSystems() {
        try {
            ClientBuilder.newClient().target(inventory_baseUri)
                        .path("inventory").path("systems")
                        .request().get();
                        
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    public Response getSystem(@PathParam("hostname") String hostname) {
        try {
            ClientBuilder.newClient().target(inventory_baseUri)
                        .path("inventory")
                        .path("systems")
                        .path(hostname)
                        .request()
                        .get();
                        
            return Response.status(Response.Status.OK)
                        .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                        .build();
        }
    }

    public Observable<Response> addProperty(String propertyName) {
        return iBuilder(webTarget().path("data"))
            // tag::rxCreateJob[]
            .rx(RxObservableInvoker.class)
            // end::rxCreateJob[]
            .post(null, new GenericType<Response>(){});
    }

    public Observable<List<String>> getProperty(String propertyName) {
        return iBuilder(webTarget().path("data").path(propertyName))
            .rx(RxObservableInvoker.class)
            .get(new GenericType<List<String>>(){});
    }

    private Invocation.Builder iBuilder(WebTarget target) {
        return target
            .request()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

    // tag::webTarget[]
    private WebTarget webTarget() {
        if (this.target == null) {
            this.target = ClientBuilder.newClient().target(baseUri)
                            .register(RxObservableInvokerProvider.class)
                            .path("gateway");
        }

        return this.target;
    }
}