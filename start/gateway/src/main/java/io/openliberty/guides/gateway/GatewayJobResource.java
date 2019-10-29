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

import io.openliberty.guides.gateway.client.JobClient;
import io.openliberty.guides.models.JobList;
import io.openliberty.guides.models.Job;
import io.openliberty.guides.models.JobResult;

@RequestScoped
@Path("/jobs")
public class GatewayJobResource {

    @Inject
    private JobClient jobClient;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<JobList> getJobs() {
        return jobClient
            .getJobs()
            .thenApplyAsync((jobs) -> {
                return new JobList(jobs.getResults());
            })
            .exceptionally((ex) -> {
                // Respond with empty list on error
                return new JobList();
            });
    }

    @GET
    @Path("{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<JobResult> getJob(@PathParam("jobId") String jobId) {
        return jobClient.getJob(jobId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<Job> createJob() {
        return jobClient.createJob();
    }
}