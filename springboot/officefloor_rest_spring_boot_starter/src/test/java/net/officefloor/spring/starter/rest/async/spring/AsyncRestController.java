package net.officefloor.spring.starter.rest.async.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/spring/async")
public class AsyncRestController {

    @Autowired
    private AsyncSpringService asyncSpringService;

    // Item 1: Spring offloads Callable execution to its MvcAsyncTaskExecutor
    @GetMapping("/callable")
    public Callable<String> callable() {
        return () -> "callable-result";
    }

    // Item 2: DeferredResult allows any thread to supply the response
    @GetMapping("/deferred")
    public DeferredResult<String> deferredResult() {
        DeferredResult<String> result = new DeferredResult<>();
        result.setResult("deferred-result");
        return result;
    }

    // Item 3: Spring awaits a CompletableFuture before writing the response
    @GetMapping("/completable")
    public CompletableFuture<String> completableFuture() {
        return CompletableFuture.completedFuture("completable-result");
    }

    // Item 4: Controller delegates to an @Async Spring service; the returned
    // CompletableFuture proves @Async proxying works end-to-end
    @GetMapping("/async-service")
    public CompletableFuture<String> asyncService() {
        return asyncSpringService.computeAsync();
    }
}
