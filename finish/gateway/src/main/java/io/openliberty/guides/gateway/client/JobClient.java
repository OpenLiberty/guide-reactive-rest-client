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

import java.util.concurrent.CompletionStage;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import javax.ws.rs.core.GenericType;

import java.util.List;
import java.util.LinkedList;
import org.apache.cxf.jaxrs.rx2.client.ObservableRxInvoker;
import org.apache.cxf.jaxrs.rx2.client.ObservableRxInvokerProvider;

import io.reactivex.rxjava3.core.*;
import io.reactivex.Observable;
//import io.reactivex.rxjava3.core.Observable;

import javax.ws.rs.client.Client;

import io.openliberty.guides.models.Job;
import io.openliberty.guides.models.JobResult;
import io.openliberty.guides.models.Jobs;

@RequestScoped
public class JobClient {

    @Inject
    @ConfigProperty(name = "GATEWAY_JOB_BASE_URI", defaultValue = "http://job-service:9080")
    private String baseUri;

    private WebTarget target;

    public JobClient() {
        this.target = null;
    }

    // tag::getJobs[]
    //public CompletionStage<Jobs> getJobs() {
    public Observable<Jobs> getJobs() {
        List<Object> providers = new LinkedList<>();
        providers.add(new ObservableRxInvokerProvider());
        Observable<Jobs> obs = iBuilder(webTarget())
            // tag::rxGetJobs[]
            .rx(ObservableRxInvoker.class)
            // end::rxGetJobs[]
            .get(new GenericType<Jobs>(){});
        return obs;
    }
    // end::getJobs[]

    // tag::getJob[]
    public Observable<JobResult> getJob(String jobId) {
        return iBuilder(webTarget().path(jobId))
            // tag::rxGetJob[]
            .rx(ObservableRxInvoker.class)
            // end::rxGetJob[]
            .get(new GenericType<JobResult>(){});
    }
    // end::getJob[]

    // tag::createJob[]
    public Observable<Job> createJob() {
        return iBuilder(webTarget())
            // tag::rxCreateJob[]
            .rx(ObservableRxInvoker.class)
            // end::rxCreateJob[]
            .post(null, new GenericType<Job>(){});
    }
    // end::createJob[]

    private Invocation.Builder iBuilder(WebTarget target) {
        return target
            .request()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

    private WebTarget webTarget() {
        if (this.target == null) {
            this.target = ClientBuilder
                .newClient()
                .target(baseUri)
                .path("/jobs");
        }

        return this.target;
    }

}
