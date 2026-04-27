package net.officefloor.spring.starter.rest.async.spring;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncSpringService {

    /**
     * Runs on Spring's task executor thread (separate from the controller thread),
     * demonstrating @Async proxy behaviour when the service is injected into a controller.
     */
    @Async
    public CompletableFuture<String> computeAsync() {
        return CompletableFuture.completedFuture("async-service-result");
    }
}
