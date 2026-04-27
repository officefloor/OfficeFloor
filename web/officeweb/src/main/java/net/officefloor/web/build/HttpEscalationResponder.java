package net.officefloor.web.build;

import java.io.IOException;

/**
 * Provides ability to send an {@link net.officefloor.frame.api.escalate.Escalation} response.
 */
public interface HttpEscalationResponder<E extends Throwable> {

    /**
     * Obtains the <code>Content-Type</code> provided by this
     * {@link HttpEscalationResponder}.
     *
     * @return <code>Content-Type</code> provided by this
     * {@link HttpEscalationResponder}.
     */
    String getContentType();

    /**
     * Sends the {@link net.officefloor.frame.api.escalate.Escalation}.
     *
     * @param context {@link HttpEscalationResponderContext}.
     * @throws IOException If fails to send the object.
     */
    void send(HttpEscalationResponderContext<E> context) throws IOException;

}
