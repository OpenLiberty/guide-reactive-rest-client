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
package io.openliberty.guides.gateway;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.openliberty.guides.gateway.client.JobClient;
import io.openliberty.guides.models.Job;
import io.openliberty.guides.models.JobList;
import io.openliberty.guides.models.JobResult;
import io.openliberty.guides.models.Jobs;
import rx.Observable;

@ApplicationScoped
@Path("/jobs")
public class GatewayJobResource {

    @Inject
    private JobClient jobClient;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JobList getJobs() throws InterruptedException {
        final Holder<List<JobResult>> holder = new Holder<List<JobResult>>();
        CountDownLatch countdownLatch =  new CountDownLatch(1);
        Observable<Jobs> obs = jobClient.getJobs();
            // tag::getJobsSubscribe[]
            obs.subscribe((v) -> {
                // tag::getJobsHolder[]
                holder.value = ((Jobs)v).getResults();
                // end::getJobsHolder[]
                // tag::getJobsCountDown[]
                countdownLatch.countDown();
                // end::getJobsCountDown[]
            });
            // end::getJobsSubscribe[]
            try {
                // tag::getJobsAwait[]
                countdownLatch.await();
                // end::getJobsAwait[]
            } catch (InterruptedException e) {
                return new JobList();
            }
            return new JobList(holder.value);
    }

    @GET
    @Path("{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public JobResult getJob(@PathParam("jobId") String jobId) throws InterruptedException {
        final Holder<JobResult> holder = new Holder<JobResult>();
        CountDownLatch countdownLatch = new CountDownLatch(1);
        Observable<JobResult> obs = jobClient.getJob(jobId);
        // tag::getJobSubscribe[]
        obs.subscribe((v) -> {
            // tag::getJobHolder[]
            holder.value = v;
            // end::getJobHolder[]
            // tag::getJobCountDown[]
            countdownLatch.countDown();
            // end::getJobCountDown[]
        });
        // end::getJobSubscribe[]
        try {
            // tag::getJobAwait[]
            countdownLatch.await();
            // end::getJobAwait[]
        } catch (InterruptedException e) {
            return new JobResult();
        }
        return holder.value;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Job createJob() throws InterruptedException {
        final Holder<Job> holder = new Holder<Job>();
        CountDownLatch countdownLatch = new CountDownLatch(1);
        Observable<Job> obs = jobClient.createJob();
        // tag::createJobSubscribe[]
        obs.subscribe((v) -> {
            // tag::createJobHolder[]
            holder.value = v;
            // end::createJobHolder[]
            // tag::createJobCountDown[]
            countdownLatch.countDown();
            // end::createJobCountDown[]
        });
        // end::createJobSubscribe[]
        try {
            // tag::createJobAwait[]
            countdownLatch.await();
            // end::createJobAwait[]
        } catch (InterruptedException e) {
            return new Job();
        }
        return holder.value;
    }

    private class Holder<T> {
        public volatile T value;
    }
}
