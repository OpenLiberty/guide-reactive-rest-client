// // tag::copyright[]
// /*******************************************************************************
//  * Copyright (c) 2020, 2021 IBM Corporation and others.
//  * All rights reserved. This program and the accompanying materials
//  * are made available under the terms of the Eclipse Public License v1.0
//  * which accompanies this distribution, and is available at
//  * http://www.eclipse.org/legal/epl-v10.html
//  *
//  * Contributors:
//  *     IBM Corporation - Initial implementation
//  *******************************************************************************/
// // end::copyright[]
// package it.io.openliberty.guides.query;

// import java.time.Duration;

// import org.mockserver.client.MockServerClient;
// import org.testcontainers.containers.KafkaContainer;
// import org.testcontainers.containers.MockServerContainer;
// import org.testcontainers.containers.Network;
// import org.testcontainers.junit.jupiter.Container;

// public class AppContainerConfig implements SharedContainerConfiguration {
//     private static Logger logger = LoggerFactory.getLogger(QueryServiceIT.class);
    
//     private static Network network = Network.newNetwork();

//     private static ImageFromDockerfile queryImage
//     = new ImageFromDockerfile("query:1.0-SNAPSHOT")
//             .withDockerfile(Paths.get("./Dockerfile"));

//     public static final DockerImageName MOCKSERVER_IMAGE = DockerImageName
//         .parse("mockserver/mockserver")
//         .withTag("mockserver-" + MockServerClient.class
//                 .getPackage().getImplementationVersion());

//     public static MockServerContainer mockServer =
//                 new MockServerContainer(MOCKSERVER_IMAGE)
//                     .withNetworkAliases("mock-server")
//                     .withNetwork(network);
//     public static MockServerClient mockClient;

//     private static KafkaContainer kafkaContainer = new KafkaContainer(
//         DockerImageName.parse("confluentinc/cp-kafka:latest"))
//             .withListener(() -> "kafka:19092")
//             .withNetwork(network);

//     private static KafkaContainer kafkaContainer = new KafkaContainer(
//         DockerImageName.parse("confluentinc/cp-kafka:latest"))
//             .withListener(() -> "kafka:19092")
//             .withNetwork(network);

//     private static GenericContainer<?> queryContainer =
//         new GenericContainer(queryImage)
//             .withNetwork(network)
//             .withExposedPorts(9080)
//             .waitingFor(Wait.forHttp("/health/ready"))
//             .withStartupTimeout(Duration.ofMinutes(3))
//             .withLogConsumer(new Slf4jLogConsumer(logger))
//             .dependsOn(kafkaContainer);

//     @Override
//     public void startContainers() {
//         mockServer.start();
//         mockClient = new MockServerClient(
//             mockServer.getHost(),
//             mockServer.getServerPort());
// System.out.println("printing mockClient in appCont");
//         System.out.println(mockClient);
//         kafkaContainer.start();

//         queryContainer.withEnv(
//             "InventoryClient/mp-rest/uri",
//                 "http://mock-server:" + MockServerContainer.PORT);
//         queryContainer.start();
//     }

// }
