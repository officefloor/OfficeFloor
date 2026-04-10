package net.officefloor.web.response;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.server.http.HttpEscalationContext;
import net.officefloor.server.http.HttpEscalationHandler;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.accept.AcceptNegotiator;
import net.officefloor.web.accept.AcceptNegotiatorBuilderImpl;
import net.officefloor.web.build.AcceptNegotiatorBuilder;
import net.officefloor.web.build.HttpEscalationResponder;
import net.officefloor.web.build.HttpEscalationResponderContext;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderContext;
import net.officefloor.web.build.HttpObjectResponderFactory;
import net.officefloor.web.build.NoAcceptHandlersException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Responder for an {@link Object} or {@link net.officefloor.frame.api.escalate.Escalation}.
 */
public class HttpResponder implements HttpEscalationHandler {

    /**
     * Obtains the default {@link HttpObjectResponderFactory}.
     */
    @FunctionalInterface
    public static interface DefaultHttpObjectResponder {

        /**
         * Obtains the default {@link HttpObjectResponderFactory}.
         *
         * @return Default {@link HttpObjectResponderFactory}.
         * @throws Exception If fails to obtain the default
         *                   {@link HttpObjectResponderFactory}.
         */
        HttpObjectResponderFactory getDefaultHttpObjectResponderFactory() throws Exception;
    }

    /**
     * {@link List} of {@link HttpObjectResponderFactory} instances.
     */
    private final List<HttpObjectResponderFactory> objectResponderFactoriesList;

    /**
     * {@link DefaultHttpObjectResponder}.
     */
    private final DefaultHttpObjectResponder defaultHttpObjectResponder;

    /**
     * {@link AcceptNegotiator} for the {@link Object} {@link ContentTypeCache}.
     */
    private AcceptNegotiator<ContentTypeCache> objectNegotiator;

    /**
     * {@link AcceptNegotiator} for the {@link Escalation} {@link ContentTypeCache}.
     */
    private AcceptNegotiator<ContentTypeCache> escalationNegotiator;

    /**
     * {@link WritableHttpHeader} instances when not acceptable type requested.
     */
    private WritableHttpHeader[] notAcceptableHeaders;

    /**
     * Instantiate.
     *
     * @param objectResponderFactories   {@link List} of
     *                                   {@link HttpObjectResponderFactory}
     *                                   instances.
     * @param defaultHttpObjectResponder {@link DefaultHttpObjectResponder}.
     */
    public HttpResponder(List<HttpObjectResponderFactory> objectResponderFactories,
                         DefaultHttpObjectResponder defaultHttpObjectResponder) {
        this.objectResponderFactoriesList = objectResponderFactories;
        this.defaultHttpObjectResponder = defaultHttpObjectResponder;
    }

    /**
     * <p>
     * Builds this {@link HttpResponder}.
     * <p>
     * This may be called multiple times if re-used.
     *
     * @throws Exception If fails to build.
     */
    public void build() throws Exception {

        // Determine if already built
        if (this.notAcceptableHeaders != null) {
            return; // already built
        }

        // Create the not acceptable headers
        StringBuilder accept = new StringBuilder();
        boolean isFirst = true;

        // Create the negotiators
        AcceptNegotiatorBuilder<ContentTypeCache> objectBuilder = new AcceptNegotiatorBuilderImpl<>(ContentTypeCache.class);
        AcceptNegotiatorBuilder<ContentTypeCache> escalationBuilder = new AcceptNegotiatorBuilderImpl<>(ContentTypeCache.class);
        NEXT_FACTORY:
        for (HttpObjectResponderFactory factory : this.objectResponderFactoriesList) {
            String contentType = factory.getContentType();
            if (CompileUtil.isBlank(contentType)) {
                continue NEXT_FACTORY;
            }

            // Add content-type for negotiator
            objectBuilder.addHandler(contentType, new ContentTypeCache(factory));
            escalationBuilder.addHandler(contentType, new ContentTypeCache(factory));

            // Include in accept header response
            if (!isFirst) {
                accept.append(", ");
            }
            isFirst = false;
            accept.append(contentType);
        }
        if (isFirst) {
            // Determine if provide default responder
            HttpObjectResponderFactory defaultFactory = this.defaultHttpObjectResponder
                    .getDefaultHttpObjectResponderFactory();
            if (defaultFactory != null) {

                // Provide default
                String contentType = defaultFactory.getContentType();
                if (!CompileUtil.isBlank(contentType)) {

                    // Add content-type for negotiator
                    objectBuilder.addHandler(contentType, new ContentTypeCache(defaultFactory));
                    escalationBuilder.addHandler(contentType, new ContentTypeCache(defaultFactory));

                    // Only the one, so is the accept type
                    accept.append(contentType);
                }
            }
        }
        try {
            this.objectNegotiator = objectBuilder.build();
            this.escalationNegotiator = escalationBuilder.build();
        } catch (NoAcceptHandlersException ex) {
            throw new Exception(
                    "Must have at least one " + HttpObjectResponderFactory.class.getSimpleName() + " configured");
        }

        // Create the not acceptable headers
        this.notAcceptableHeaders = new WritableHttpHeader[]{
                new WritableHttpHeader(new HttpHeaderName("accept"), new HttpHeaderValue(accept.toString()))};
    }

    /**
     * Sends the {@link Object}.
     *
     * @param object                    {@link Object} to send.
     * @param httpStatus                {@link HttpStatus}.
     * @param connection                {@link ServerHttpConnection}.
     * @param managedFunctionType       {@link ManagedFunctionType} of {@link net.officefloor.frame.api.function.ManagedFunction} sending the {@link Object}.
     * @param managedFunctionObjectType {@link ManagedFunctionObjectType} of the {@link ObjectResponse} on the {@link net.officefloor.frame.api.function.ManagedFunction}.
     */
    public void sendObject(Object object, HttpStatus httpStatus, ServerHttpConnection connection,
                           ManagedFunctionType<?, ?> managedFunctionType, ManagedFunctionObjectType<?> managedFunctionObjectType) {

        // Lazy obtain the content type cache
        ContentTypeCache[] contentTypeCache = this.objectNegotiator.getHandler(connection.getRequest());

        // Ensure have acceptable media types
        if (contentTypeCache == null) {

            // Obtain the requested accept media types
            StringBuilder responseEntity = new StringBuilder("Accept media types not supported: ");
            boolean isFirst = true;
            for (HttpHeader headers : connection.getRequest().getHeaders().getHeaders("accept")) {
                if (!isFirst) {
                    responseEntity.append(", ");
                }
                isFirst = false;
                responseEntity.append(headers.getValue());
            }

            // Propagate unsupported media/type
            throw new HttpException(HttpStatus.NOT_ACCEPTABLE, this.notAcceptableHeaders, responseEntity.toString());
        }

        // Provide response status
        connection.getResponse().setStatus(httpStatus);

        // Send the object
        this.sendObject(object, contentTypeCache, OBJECT_RESPONDER_FACTORY, connection,
                managedFunctionType, managedFunctionObjectType);
    }

    /*
     * ==================== HttpEscalationHandler ====================
     */

    @Override
    public boolean handle(HttpEscalationContext context) throws IOException {

        // Obtain the connection
        ServerHttpConnection connection = context.getServerHttpConnection();

        // Obtain the acceptable content type
        ContentTypeCache[] contentTypeCache = this.escalationNegotiator.getHandler(connection.getRequest());
        if (contentTypeCache == null) {
            return false; // not able to handle escalation
        }

        // Obtain the escalation
        Throwable escalation = context.getEscalation();

        // Send escalation (escalation handled in isolation away from managed function)
        this.sendObject(escalation, contentTypeCache, ESCALATION_RESPONDER_FACTORY, connection, null, null);
        return true; // handled
    }

    /*
     * ========================= Common ===============================
     */

    /**
     * Object {@link ResponderFactory}.
     */
    private final ResponderFactory OBJECT_RESPONDER_FACTORY = new ResponderFactory() {

        @Override
        public <T> Responder<T> createResponder(Class<T> objectType, HttpObjectResponderFactory factory) {
            HttpObjectResponder<T> objectResponder = factory.createHttpObjectResponder(objectType);
            return (objectResponder == null) ? null : (object, connection, managedFunctionType, managedFunctionObjectType) -> {
                objectResponder.send(new HttpObjectResponderContext<T>() {

                    @Override
                    public T getResponseObject() {
                        return object;
                    }

                    @Override
                    public ServerHttpConnection getServerHttpConnection() {
                        return connection;
                    }

                    @Override
                    public ManagedFunctionType<?, ?> getManagedFunctionType() {
                        return managedFunctionType;
                    }

                    @Override
                    public ManagedFunctionObjectType<?> getManagedFunctionObjectType() {
                        return managedFunctionObjectType;
                    }
                });
            };
        }
    };

    /**
     * {@link Escalation} {@link ResponderFactory}.
     */
    private static ResponderFactory ESCALATION_RESPONDER_FACTORY = new ResponderFactory() {
        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <E> Responder<E> createResponder(Class<E> objectType, HttpObjectResponderFactory factory) {
            Class escalationType = objectType;
            boolean isOfficeFloorEscalation = HttpException.class.isAssignableFrom(escalationType);
            HttpEscalationResponder escalationResponder = factory.createHttpEscalationResponder(escalationType, isOfficeFloorEscalation);
            return (escalationResponder == null) ? null : (object, connection, managedFunctionType, managedFunctionObjectType) -> {
                escalationResponder.send(new HttpEscalationResponderContext() {

                    @Override
                    public Throwable getEscalation() {
                        return (Throwable) object;
                    }

                    @Override
                    public boolean isOfficeFloorEscalation() {
                        return isOfficeFloorEscalation;
                    }

                    @Override
                    public ServerHttpConnection getServerHttpConnection() {
                        return connection;
                    }
                });
            };
        }
    };

    /**
     * Responder factory.
     */
    @FunctionalInterface
    private static interface ResponderFactory {

        /**
         * <p>
         * Creates the {@link Responder}.
         * <p>
         * Should this {@link Responder} not support the object type, it should return <code>null</code>.
         *
         * @param objectType Object type.
         * @param factory    {@link HttpObjectResponderFactory}.
         * @return {@link HttpObjectResponder} or <code>null</code> if not support the object type.
         */
        <T> Responder<T> createResponder(Class<T> objectType, HttpObjectResponderFactory factory);
    }

    /**
     * Responder.
     */
    @FunctionalInterface
    private static interface Responder<T> {

        /**
         * Sends the response.
         *
         * @param object                    Object for response.
         * @param connection                {@link ServerHttpConnection}.
         * @param managedFunctionType       {@link ManagedFunctionType}.
         * @param managedFunctionObjectType {@link ManagedFunctionObjectType}.
         * @throws IOException If fails to send response.
         */
        void send(T object, ServerHttpConnection connection, ManagedFunctionType<?, ?> managedFunctionType,
                  ManagedFunctionObjectType<?> managedFunctionObjectType) throws IOException;
    }

    /**
     * Sends the object.
     *
     * @param object           Object for the response.
     * @param contentTypeCache {@link ContentTypeCache} instances.
     * @param responderFactory {@link ResponderFactory}.
     * @param connection       {@link ServerHttpConnection} connection.
     * @return <code>true</code> if object sent.
     */
    @SuppressWarnings("unchecked")
    private <T> void sendObject(T object, ContentTypeCache[] contentTypeCache, ResponderFactory responderFactory,
                                ServerHttpConnection connection, ManagedFunctionType<?, ?> managedFunctionType,
                                ManagedFunctionObjectType<?> managedFunctionObjectType) {

        // Obtain the object type
        Class<T> objectType = (Class<T>) object.getClass();

        // Find the corresponding type
        Responder<T> responder = null;
        FIND_RESPONDER:
        for (ContentTypeCache contentType : contentTypeCache) {
            for (int j = 0; j < contentType.responders.length; j++) {
                ObjectResponderCache<?> cachedResponder = contentType.responders[j];
                if (cachedResponder.objectType == objectType) {
                    responder = (Responder<T>) cachedResponder.responder;
                    break FIND_RESPONDER;
                }
            }
        }
        if (responder == null) {
            // Need to create object responder for type
            CREATED_RESPONDER:
            for (ContentTypeCache contentType : contentTypeCache) {

                // Attempt to create responder
                responder = responderFactory.createResponder(objectType, contentType.factory);
                if (responder != null) {

                    // Load the cached responder
                    ObjectResponderCache<T> cachedResponder = new ObjectResponderCache<>(objectType, responder);

                    // Append the cached responder to cache
                    ObjectResponderCache<?>[] responders = Arrays.copyOf(contentType.responders, contentType.responders.length + 1);
                    responders[responders.length - 1] = cachedResponder;
                    contentType.responders = responders;

                    // Created
                    break CREATED_RESPONDER;
                }
            }
        }

        // Ensure have object responder
        if (responder == null) {
            String accept = contentTypeCache[0].factory.getContentType(); // Should always be one
            throw new HttpException(HttpStatus.NOT_ACCEPTABLE, this.notAcceptableHeaders,
                    "Media type " + accept + " supported but not for particular response type");
        }

        // Send the response
        try {
            responder.send(object, connection, managedFunctionType, managedFunctionObjectType);
        } catch (IOException ex) {
            throw new HttpException(ex);
        }
    }

    /**
     * <code>content-type</code> cache object.
     */
    private static class ContentTypeCache {

        /**
         * {@link HttpObjectResponderFactory}.
         */
        private final HttpObjectResponderFactory factory;

        /**
         * {@link ObjectResponderCache} items.
         */
        private ObjectResponderCache<?>[] responders = new ObjectResponderCache[0];

        /**
         * Instantiate.
         *
         * @param factory {@link HttpObjectResponderFactory} for the
         *                <code>content-type</code>.
         */
        private ContentTypeCache(HttpObjectResponderFactory factory) {
            this.factory = factory;
        }
    }

    /**
     * {@link ObjectResponse} cache object.
     */
    private static class ObjectResponderCache<T> {

        /**
         * Object type.
         */
        private final Class<T> objectType;

        /**
         * ObjectResponder
         */
        private final Responder<T> responder;

        /**
         * Instantiate.
         *
         * @param objectType Object type.
         * @param responder  {@link Responder} for the object type.
         */
        private ObjectResponderCache(Class<T> objectType, Responder<T> responder) {
            this.objectType = objectType;
            this.responder = responder;
        }
    }

}
