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
package it.io.openliberty.guides.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.Socket;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

public class QueryServiceIT {

    private static Logger logger = LoggerFactory.getLogger(QueryServiceIT.class);

    public static QueryResourceClient client;

    private static boolean isServiceRunning;
    private static Network network = createNetwork();

    private static String testHost1 =
        "{"
            + "\"hostname\" : \"testHost1\","
            + "\"systemLoad\" : 1.23"
        + "}";
    private static String testHost2 =
        "{"
            + "\"hostname\" : \"testHost2\","
            + "\"systemLoad\" : 3.21"
        + "}";
    private static String testHost3 =
        "{" + "\"hostname\" : \"testHost3\","
            + "\"systemLoad\" : 2.13"
        + "}";

    private static ImageFromDockerfile queryImage =
        new ImageFromDockerfile("query:1.0-SNAPSHOT")
            .withDockerfile(Paths.get("./Dockerfile"));

    public static final DockerImageName MOCKSERVER_IMAGE =
        DockerImageName.parse("mockserver/mockserver")
            .withTag("mockserver-"
                + MockServerClient.class.getPackage().getImplementationVersion());

    public static MockServerContainer mockServer =
        new MockServerContainer(MOCKSERVER_IMAGE)
            .withNetworkAliases("mock-server")
            .withNetwork(network);

    public static MockServerClient mockClient;

    private static GenericContainer<?> queryContainer =
        new GenericContainer(queryImage)
            .withNetwork(network)
            .withExposedPorts(9080)
            .waitingFor(Wait.forLogMessage("^.*CWWKF0011I.*$", 1))
            .withStartupTimeout(Duration.ofMinutes(3))
            .withLogConsumer(new Slf4jLogConsumer(logger))
            .dependsOn(mockServer);

    private static QueryResourceClient createRestClient(String urlPath) {
        ClientConfig config = new ClientConfig();
        JerseyClient jerseyClient = JerseyClientBuilder.createClient(config);
        JerseyWebTarget target = jerseyClient.target(urlPath);
        return WebResourceFactory.newResource(QueryResourceClient.class, target);
    }

    private static boolean isServiceRunning(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static Network createNetwork() {
        if (isServiceRunning("localhost", 9080)) {
            isServiceRunning = true;
            return new Network() {

                @Override
                public Statement apply(Statement base, Description description) {
                    return null;
                }

                @Override
                public String getId() {
                    return "reactive-app";
                }

                @Override
                public void close() {
                }
            };
        } else {
            isServiceRunning = false;
            return Network.newNetwork();
        }
    }

    @BeforeAll
    public static void startContainers() {
        mockServer.start();
        mockClient = new MockServerClient(
            mockServer.getHost(),
            mockServer.getServerPort());
        String urlPath;
        if (isServiceRunning) {
            System.out.println("Testing with mvn liberty:devc");
            urlPath = "http://localhost:9080";
        } else {
            System.out.println("Testing with mvn verify");
            queryContainer.withEnv(
                "INVENTORY_BASE_URI",
                "http://mock-server:" + MockServerContainer.PORT);
            queryContainer.start();
            urlPath = "http://"
                      + queryContainer.getHost()
                      + ":" + queryContainer.getFirstMappedPort();
        }

        System.out.println("Creating REST client with: " + urlPath);
        client = createRestClient(urlPath);
    }

    @BeforeEach
    public void setup() throws InterruptedException {
        mockClient.when(HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/inventory/systems"))
                    .respond(HttpResponse.response()
                        .withStatusCode(200)
                        .withBody("[\"testHost1\","
                                  + "\"testHost2\","
                                  + "\"testHost3\"]")
                        .withHeader("Content-Type", "application/json"));

        mockClient.when(HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/inventory/systems/testHost1"))
                    .respond(HttpResponse.response()
                        .withStatusCode(200)
                        .withBody(testHost1)
                        .withHeader("Content-Type", "application/json"));

        mockClient.when(HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/inventory/systems/testHost2"))
                    .respond(HttpResponse.response()
                        .withStatusCode(200)
                        .withBody(testHost2)
                        .withHeader("Content-Type", "application/json"));

        mockClient.when(HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/inventory/systems/testHost3"))
                    .respond(HttpResponse.response()
                        .withStatusCode(200)
                        .withBody(testHost3)
                        .withHeader("Content-Type", "application/json"));
    }

    @AfterAll
    public static void stopContainers() {
        if (!isServiceRunning) {
            queryContainer.stop();
        }
        mockClient.close();
        mockServer.stop();
        network.close();
    }

    // tag::testSystemLoad[]
    @Test
    public void testSystemLoad() {
        Map<String, Properties> response = client.systemLoad();
        assertEquals(
            "testHost2",
            response.get("highest").get("hostname"),
            "Returned highest system load incorrect"
        );
        assertEquals(
            "testHost1",
            response.get("lowest").get("hostname"),
            "Returned lowest system load incorrect"
        );
    }
    // end::testSystemLoad[]
}
