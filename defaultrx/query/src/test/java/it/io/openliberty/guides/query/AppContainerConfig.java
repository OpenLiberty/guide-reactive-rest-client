/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
// end::copyright[]
package it.io.openliberty.guides.query;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import org.testcontainers.containers.Network;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertEquals;

private static Logger logger = LoggerFactory.getLogger(AppContainerConfig.class);

private static Network network = Network.newNetwork();

public static String restClientClass = "InventoryClient.class";

private static ImageFromDockerfile queryImage
    = new ImageFromDockerfile("query:1.0-SNAPSHOT")
            .withDockerfile(Paths.get("./Dockerfile"));

    public static final DockerImageName MOCKSERVER_IMAGE = DockerImageName
        .parse("mockserver/mockserver")
        .withTag("mockserver-" + MockServerClient.class
                .getPackage().getImplementationVersion());

    public static MockServerContainer mockServer =
                new MockServerContainer(MOCKSERVER_IMAGE)
                    .withNetworkAliases("mock-server")
                    .withNetwork(network);
    public static MockServerClient mockClient;

    private static KafkaContainer kafkaContainer = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withListener(() -> "kafka:19092")
            .withNetwork(network);

    private static GenericContainer<?> queryContainer =
        new GenericContainer(queryImage)
            .withNetwork(network)
            .withExposedPorts(9080)
            .waitingFor(Wait.forHttp("/health/ready"))
            .withStartupTimeout(Duration.ofMinutes(3))
            .withLogConsumer(new Slf4jLogConsumer(logger))
            .dependsOn(kafkaContainer);

    @Override
    public static void startContainers() {
        mockServer.start();
        mockClient = new MockServerClient(
            mockServer.getHost(),
            mockServer.getServerPort());

        kafkaContainer.start();

        queryContainer.withEnv(
            "INVENTORY_BASE_URI",
                "http://mock-server:" + MockServerContainer.PORT);
        queryContainer.start();
    }
