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
package io.openliberty.guides.query;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.openliberty.guides.query.client.*;
import io.openliberty.guides.models.SystemLoad;
import rx.Observable;

@ApplicationScoped
@Path("/query")
public class QueryResource {
    
    @Inject
    private InventoryClient inventoryClient;

    private List<String> getSystems() {
        List<String> obs = inventoryClient.getSystems();
        System.out.println(obs);
        return obs;
    }

    @GET
    @Path("/systemLoad")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Properties> systemLoad() {
        List<String> systems = getSystems();
        CountDownLatch remainingSystems = new CountDownLatch(systems.size());
        final Holder systemLoads = new Holder();

        for (String system : systems) {
            inventoryClient.getSystem(system)
                           .subscribe(p -> {
                                if (p != null) {
                                    systemLoads.updateHighest(p);
                                    systemLoads.updateLowest(p);
                                    remainingSystems.countDown();
                                }
                           });
        }

        try {
            remainingSystems.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return systemLoads.values;
    }

    private class Holder2<T> {
        @SuppressWarnings("unchecked")
        public volatile T value;
    }

    private class Holder {
        @SuppressWarnings("unchecked")
        // tag::volatile
        public volatile Map<String, Properties> values;
        // end::volatile

        public Holder() {
            // tag::concurrentHashMap
            this.values = new ConcurrentHashMap<String, Properties>();
            // end::concurrentHashMap
            
            // Initialize highest and lowest values
            this.values.put("highest", new Properties());
            this.values.put("lowest", new Properties());
            this.values.get("highest").put("systemLoad", new BigDecimal(Double.MIN_VALUE));
            this.values.get("lowest").put("systemLoad", new BigDecimal(Double.MAX_VALUE));
        }

        public void updateHighest(Properties p) {
            BigDecimal load = (BigDecimal) p.get("systemLoad");
            BigDecimal highest = (BigDecimal) this.values
                                                  .get("highest")
                                                  .get("systemLoad");
            if (load.compareTo(highest) > 0) {
                this.values.put("highest", p);
            }
        }

        public void updateLowest(Properties p) {
            BigDecimal load = (BigDecimal) p.get("systemLoad");
            BigDecimal lowest = (BigDecimal) this.values
                                                 .get("lowest")
                                                 .get("systemLoad");
            if (load.compareTo(lowest) < 0) {
                this.values.put("lowest", p);
            }
        }
    }
}
