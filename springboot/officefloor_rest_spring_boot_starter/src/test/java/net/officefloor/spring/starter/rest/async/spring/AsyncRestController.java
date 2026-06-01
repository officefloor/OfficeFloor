/*-
 * #%L
 * OfficeFloor REST Spring Boot Starter
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
