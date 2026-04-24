package net.officefloor.spring.starter.rest.async;

import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public abstract class AbstractAsyncVerification extends AbstractMockMvcVerification {

    /**
     * Performs a GET and resolves async dispatch if needed, allowing Spring's
     * Callable/DeferredResult/CompletableFuture and OfficeFloor's own async model
     * to be verified with the same assertions.
     */
    private MvcResult asyncGet(String subPath) throws Exception {
        MvcResult mvcResult = this.mvc.perform(get(getPath(subPath))).andReturn();
        if (mvcResult.getRequest().isAsyncStarted()) {
            mvcResult.getAsyncResult(2000);
            mvcResult = this.mvc.perform(asyncDispatch(mvcResult)).andReturn();
        }
        return mvcResult;
    }

    // ── Item 1: Callable<T> — Spring offloads execution to a task executor ────

    @Test
    public void callable() throws Exception {
        MvcResult result = asyncGet("/callable");
        MockHttpServletResponse r = result.getResponse();
        assertEquals(200, r.getStatus());
        assertEquals("callable-result", r.getContentAsString());
    }

    // ── Item 2: DeferredResult<T> — result set from an external thread ────────

    @Test
    public void deferredResult() throws Exception {
        MvcResult result = asyncGet("/deferred");
        MockHttpServletResponse r = result.getResponse();
        assertEquals(200, r.getStatus());
        assertEquals("deferred-result", r.getContentAsString());
    }

    // ── Item 3: CompletableFuture<T> — Spring awaits the future ──────────────

    @Test
    public void completableFuture() throws Exception {
        MvcResult result = asyncGet("/completable");
        MockHttpServletResponse r = result.getResponse();
        assertEquals(200, r.getStatus());
        assertEquals("completable-result", r.getContentAsString());
    }
}
