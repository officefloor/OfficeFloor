package net.officefloor.web.build;

import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Context for the {@link HttpObjectResponder}.
 */
public interface HttpObjectResponderContext<T> {

    /**
     * Obtains the response {@link Object} being sent.
     *
     * @return Response {@link Object} being sent.
     */
    T getResponseObject();

    /**
     * Obtains the {@link ServerHttpConnection}.
     *
     * @return {@link ServerHttpConnection}.
     */
    ServerHttpConnection getServerHttpConnection();

    /**
     * Obtains the {@link ManagedFunctionType} that is using the {@link net.officefloor.web.ObjectResponse}.
     *
     * @return {@link ManagedFunctionType} that is using the {@link net.officefloor.web.ObjectResponse}.
     */
    ManagedFunctionType<?, ?> getManagedFunctionType();

    /**
     * Obtains the {@link ManagedFunctionObjectType} on the {@link net.officefloor.frame.api.function.ManagedFunction}
     * requiring the {@link net.officefloor.web.ObjectResponse}.
     *
     * @return {@link ManagedFunctionObjectType}.
     */
    ManagedFunctionObjectType<?> getManagedFunctionObjectType();

}
