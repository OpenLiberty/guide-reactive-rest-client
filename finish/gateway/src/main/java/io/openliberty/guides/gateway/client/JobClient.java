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
package io.openliberty.guides.gateway.client;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvokerProvider;

import io.openliberty.guides.models.Job;
import io.openliberty.guides.models.JobResult;
import io.openliberty.guides.models.Jobs;
import rx.Observable;

@RequestScoped
public class JobClient {

    @Inject
    @ConfigProperty(name = "GATEWAY_JOB_BASE_URI", defaultValue = "http://localhost:9082")
    private String baseUri;

    private WebTarget target;

    public JobClient() {
        this.target = null;
    }

    // tag::getJobs[]
    public Observable<Jobs> getJobs() {
        return iBuilder(webTarget())
            // tag::rxGetJobs[]
            .rx(RxObservableInvoker.class)
            // end::rxGetJobs[]
            .get(new GenericType<Jobs>() {});
    }
    // end::getJobs[]

    // tag::getJob[]
    public Observable<JobResult> getJob(String jobId) {
        return iBuilder(webTarget().path(jobId))
            // tag::rxGetJob[]
            .rx(RxObservableInvoker.class)
            // end::rxGetJob[]
            .get(new GenericType<JobResult>(){});
    }
    // end::getJob[]

    // tag::createJob[]
    public Observable<Job> createJob() {
        return iBuilder(webTarget())
            // tag::rxCreateJob[]
            .rx(RxObservableInvoker.class)
            // end::rxCreateJob[]
            .post(null, new GenericType<Job>(){});
    }
    // end::createJob[]


    private Invocation.Builder iBuilder(WebTarget target) {
        return target
            .request()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

    // tag::webTarget[]
    private WebTarget webTarget() {
        if (this.target == null) {
            this.target = ClientBuilder
                .newClient()
                .target(baseUri)
                // tag::register[]
                .register(RxObservableInvokerProvider.class)
                // end::register[]
                .path("/jobs");
        }

        return this.target;
    }
    // end::webTarget[]
}
