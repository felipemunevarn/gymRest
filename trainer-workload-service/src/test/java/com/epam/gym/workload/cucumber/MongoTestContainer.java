package com.epam.gym.workload.cucumber;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoTestContainer {

    public static final MongoDBContainer CONTAINER;

    static {
        CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));
        CONTAINER.start();
        System.out.println(">>> MongoDB TestContainer started at: " + CONTAINER.getReplicaSetUrl());
    }

    private MongoTestContainer() {}
}

