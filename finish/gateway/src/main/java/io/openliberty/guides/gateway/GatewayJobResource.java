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

import java.util.concurrent.CompletionStage;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.util.List;

import rx.Observable;

import io.openliberty.guides.gateway.client.JobClient;
import io.openliberty.guides.models.JobList;
import io.openliberty.guides.models.Job;
import io.openliberty.guides.models.JobResult;
import io.openliberty.guides.models.Jobs;

@RequestScoped
@Path("/jobs")
public class GatewayJobResource {

    @Inject
    private JobClient jobClient;

    /*@GET
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<JobList> getJobs() {
        return jobClient
            .getJobs()
            // tag::thenApplyAsync[]
            .thenApplyAsync((jobs) -> {
                return new JobList(jobs.getResults());
            })
            // end::thenApplyAsync[]
            // tag::exceptionally[]
            .exceptionally((ex) -> {
                // Respond with empty list on error
                return new JobList();
            });
            // end::exceptionally[]
    }*/

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JobList getJobs() throws InterruptedException {
        final Holder<List<JobResult>> holder = new Holder<List<JobResult>>();
        CountDownLatch cdLatch =  new CountDownLatch(1);
        Observable<Jobs> obs = jobClient.getJobs();
            obs
                .subscribe((v) -> {
                    holder.value = ((Jobs)v).getResults();
                    cdLatch.countDown();
            });

            //Wait for results to be available
            try {
                cdLatch.await();
            } catch (InterruptedException e) {}

            return new JobList(holder.value);
    }

    /*@GET
    @Path("{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<JobResult> getJob(@PathParam("jobId") String jobId) {
        return jobClient.getJob(jobId);
    }*/

    @GET
    @Path("{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public JobResult getJob(@PathParam("jobId") String jobId) throws InterruptedException {
        final Holder<JobResult> holder = new Holder<JobResult>();
        CountDownLatch cdLatch = new CountDownLatch(1);
        Observable<JobResult> obs = jobClient.getJob(jobId);
        obs
            .subscribe((v) -> {
                holder.value = v;
                cdLatch.countDown();
            });
        try {
            cdLatch.await();
        } catch (InterruptedException e) {

        }
        return holder.value;
    }

    /*@POST
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<Job> createJob() {
        return jobClient.createJob();
    }*/

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Job createJob() throws InterruptedException {
        final Holder<Job> holder = new Holder<Job>();
        CountDownLatch cdLatch = new CountDownLatch(1); //Countdown one other thread
        Observable<Job> obs = jobClient.createJob();
        obs
            .subscribe((v) -> {
                holder.value = v;
                cdLatch.countDown();
            });
        try {
            cdLatch.await();
        } catch (InterruptedException e) {
            
        }
        return holder.value;
    }

    private class Holder<T> {
        public volatile T value;
    }
}
