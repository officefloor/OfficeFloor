/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.web.response;

import java.io.IOException;
import java.util.Arrays;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
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
import net.officefloor.web.accept.AcceptNegotiatorBuilderImpl;
import net.officefloor.web.build.AcceptNegotiatorBuilder;
import net.officefloor.web.build.HttpEscalationResponder;
import net.officefloor.web.build.HttpEscalationResponderContext;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderContext;
import net.officefloor.web.build.HttpObjectResponderFactory;
import net.officefloor.web.build.NoAcceptHandlersException;

/**
 * {@link ManagedObjectSource} for the {@link ObjectResponse}.
 *
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class ObjectResponseManagedObjectSource
        extends AbstractManagedObjectSource<ObjectResponseManagedObjectSource.ObjectResponseDependencies, None> {

    /**
     * Dependency keys.
     */
    public static enum ObjectResponseDependencies {
        SERVER_HTTP_CONNECTION
    }

    /**
     * {@link HttpResponder}.
     */
    private final HttpResponder httpResponder;

    /**
     * Response {@link HttpStatus}.
     */
    private final HttpStatus httpStatus;

    /**
     * {@link ManagedFunctionType}.
     */
    private final ManagedFunctionType<?, ?> managedFunctionType;

    /**
     * {@link ManagedFunctionObjectType}.
     */
    private final ManagedFunctionObjectType<?> managedFunctionObjectType;


    /**
     * Instantiate.
     *
     * @param httpResponder             {@link HttpResponder}.
     * @param httpStatus                {@link HttpStatus}.
     * @param managedFunctionType       {@link ManagedFunctionType}.
     * @param managedFunctionObjectType {@link ManagedFunctionObjectType}.
     */
    public ObjectResponseManagedObjectSource(HttpResponder httpResponder,
                                             HttpStatus httpStatus,
                                             ManagedFunctionType<?, ?> managedFunctionType,
                                             ManagedFunctionObjectType<?> managedFunctionObjectType) {
        this.httpResponder = httpResponder;
        this.httpStatus = httpStatus;
        this.managedFunctionType = managedFunctionType;
        this.managedFunctionObjectType = managedFunctionObjectType;
    }

    /*
     * ==================== ManagedObjectSource ======================
     */

    @Override
    protected void loadSpecification(SpecificationContext context) {
    }

    @Override
    protected void loadMetaData(MetaDataContext<ObjectResponseDependencies, None> context) throws Exception {

        // Load the meta-data
        context.setObjectClass(ObjectResponse.class);
        context.setManagedObjectClass(ObjectResponseManagedObject.class);
        context.addDependency(ObjectResponseDependencies.SERVER_HTTP_CONNECTION, ServerHttpConnection.class);

        // Ensure the HTTP responder is built
        this.httpResponder.build();
    }

    @Override
    protected ManagedObject getManagedObject() throws Throwable {
        return new ObjectResponseManagedObject<Object>();
    }

    /**
     * {@link ObjectResponse} {@link ManagedObject}.
     */
    private class ObjectResponseManagedObject<T> implements ContextAwareManagedObject,
            CoordinatingManagedObject<ObjectResponseDependencies>, ObjectResponse<T> {

        /**
         * {@link ManagedObjectContext}.
         */
        private ManagedObjectContext context;

        /**
         * {@link ServerHttpConnection}.
         */
        private ServerHttpConnection connection;

        /*
         * ==================== ManagedObject =======================
         */

        @Override
        public void setManagedObjectContext(ManagedObjectContext context) {
            this.context = context;
        }

        @Override
        public void loadObjects(ObjectRegistry<ObjectResponseDependencies> registry) throws Throwable {

            // Obtain the server HTTP connection
            this.connection = (ServerHttpConnection) registry
                    .getObject(ObjectResponseDependencies.SERVER_HTTP_CONNECTION);
        }

        @Override
        public Object getObject() throws Throwable {
            return this;
        }

        /*
         * ==================== ObjectResponse =======================
         */

        @Override
        public void send(T object) throws HttpException {
            this.context.run(() -> {

                // Easy access to source
                ObjectResponseManagedObjectSource source = ObjectResponseManagedObjectSource.this;

                // Send the object
                source.httpResponder.sendObject(object, source.httpStatus, this.connection,
                        source.managedFunctionType, source.managedFunctionObjectType);
                return null; // nothing to return
            });
        }
    }

}
