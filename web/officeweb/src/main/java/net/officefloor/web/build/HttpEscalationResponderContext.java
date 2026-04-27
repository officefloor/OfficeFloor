package net.officefloor.web.build;

import net.officefloor.server.http.ServerHttpConnection;

/**
 * Context for the {@link HttpEscalationResponder}.
 */
public interface HttpEscalationResponderContext<E extends Throwable> {

    /**
     * Obtains the response {@link net.officefloor.frame.api.escalate.Escalation} being sent.
     *
     * @return Response {@link net.officefloor.frame.api.escalate.Escalation} being sent.
     */
    E getEscalation();

    /**
     * <p>
     * Indicates if the {@link net.officefloor.frame.api.escalate.Escalation} is handled by {@link net.officefloor.frame.api.manage.OfficeFloor}.
     * <p>
     * Allows custom handling of the {@link net.officefloor.frame.api.escalate.Escalation}.
     *
     * @return <code>true</code> if handled by {@link net.officefloor.frame.api.manage.OfficeFloor}.
     */
    boolean isOfficeFloorEscalation();

    /**
     * Obtains the {@link ServerHttpConnection}.
     *
     * @return {@link ServerHttpConnection}.
     */
    ServerHttpConnection getServerHttpConnection();

}
