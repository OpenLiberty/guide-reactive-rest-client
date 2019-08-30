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


import io.openliberty.guides.models.Job;
import io.openliberty.guides.models.JobResult;
import io.openliberty.guides.models.Jobs;

@RequestScoped
public class JobGatewayClient {

    @Inject
    @ConfigProperty(name = "GATEWAY_JOB_BASE_URI", defaultValue = "http://job-service:9080")
    private String baseUri;

    private WebTarget target;

    public JobGatewayClient() {
        this.target = null;
    }

    // tag::getJobs[]
    public CompletionStage<Jobs> getJobs() {
        return iBuilder(webTarget())
            // tag::rxGetJobs[]
            .rx()
            // end::rxGetJobs[]
            .get(Jobs.class);
    }
    // end::getJobs[]

    // tag::getJob[]
    public CompletionStage<JobResult> getJob(String jobId) {
        return iBuilder(webTarget().path(jobId))
            // tag::rxGetJob[]
            .rx()
            // end::rxGetJob[]
            .get(JobResult.class);
    }
    // end::getJob[]

    // tag::createJob[]
    public CompletionStage<Job> createJob() {
        return iBuilder(webTarget())
            // tag::rxCreateJob[]
            .rx()
            // end::rxCreateJob[]
            .post(null, Job.class);
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
