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
package it.io.openliberty.guides.system;

import org.microshed.testing.SharedContainerConfiguration;
import org.microshed.testing.testcontainers.MicroProfileApplication;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;

public class AppContainerConfig implements SharedContainerConfiguration {

	private static Network network = Network.newNetwork();
	
	@Container
    public static MicroProfileApplication app = new MicroProfileApplication()
                    .withAppContextRoot("/")
                    .withNetwork(network);

    @Container
    public static GenericContainer<?> zookeeper = new GenericContainer<>("bitnami/zookeeper:3")
                    .withNetworkAliases("zookeeper")
                    .withNetwork(network)
                    .withExposedPorts(2181)
                    .withEnv("ALLOW_ANONYMOUS_LOGIN", "yes");
    
    @Container
    public static GenericContainer<?> kafka = new GenericContainer<>("bitnami/kafka:2.3.0-debian-9-r68")
                    .withNetworkAliases("kafka")
                    .withNetwork(network)
                    .withExposedPorts(9092)
                    .withEnv("KAFKA_CFG_ZOOKEEPER_CONNECT", "zookeeper:2181")
                    .withEnv("ALLOW_PLAINTEXT_LISTENER", "yes")
                    .withEnv("KAFKA_CFG_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092");
    
    @Override
    public void startContainers() {
    	zookeeper.start();
    	kafka.start();
    	app.start();
    }
    
}
