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
package io.openliberty.guides.gateway;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.openliberty.guides.gateway.client.InventoryClient;
import io.openliberty.guides.models.SystemLoad;
import io.openliberty.guides.models.PropertyMessage;
import rx.Observable;

@ApplicationScoped
@Path("/inventory")
public class GatewayResource {

    @Inject
    @RestClient
    private InventoryClient inventoryClient;

    private class Holder<T> {
        public volatile T value;
    }

    @GET
    @Path("/systems")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SystemLoad> getSystems() throws InterruptedException {
        final Holder<List<SystemLoad>> holder = new Holder<List<SystemLoad>>();
        final CountDownLatch countdownLatch =  new CountDownLatch(1);
        final Observable<SystemLoad> obs = inventoryClient.getSystems();
        obs.subscribe((v) -> {
            final List<SystemLoad> li = Arrays.asList(new SystemLoad(v.hostname, v.loadAverage));
            holder.value = li;
            countdownLatch.countDown();
        });
        try {
            countdownLatch.await();
        } catch (final InterruptedException e) {
            return new ArrayList<>();
        }
        return holder.value;
    }

    @GET
    @Path("/systems/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public SystemLoad getSystem(@PathParam("hostname") final String hostname) {
        final Holder<SystemLoad> holder = new Holder<SystemLoad>();
        CountDownLatch countdownLatch = new CountDownLatch(1);
        Observable<SystemLoad> obs = inventoryClient.getSystem(hostname);
        obs.subscribe((v) -> {
            holder.value = v;
            countdownLatch.countDown();
        });
        try {
            countdownLatch.await();
        } catch (InterruptedException e) {
            return new SystemLoad();
        }
        return holder.value;
    }

    @POST
    @Path("/systems/properties")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addProperties(final ArrayList<String> properties) {
        for (final String property : properties) {
            inventoryClient.addProperty(property);
        }

        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    PropertyMessage resetSystems() {
        final Holder<PropertyMessage> holder = new Holder<PropertyMessage>();
        CountDownLatch countdownLatch = new CountDownLatch(1);
        Observable<PropertyMessage> obs = inventoryClient.resetSystems();
        obs.subscribe((v) -> {
            holder.value = v;
            countdownLatch.countDown();
        });
        try {
            countdownLatch.await();
        } catch (InterruptedException e) {
            return new PropertyMessage();
        }
        return holder.value;
    }
}
