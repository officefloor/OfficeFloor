/*-
 * #%L
 * HTTP Server
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

package net.officefloor.server.http.impl;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.server.http.HttpExternalResponse;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * {@link net.officefloor.frame.api.managedobject.source.ManagedObjectSource} for the {@link HttpExternalResponse}.
 */
public class HttpExternalResponseManagedObjectSource extends AbstractManagedObjectSource<HttpExternalResponseManagedObjectSource.DependencyKeys, None> {

    /**
     * Dependency keys.
     */
    public static enum DependencyKeys {
        /** Server HTTP connection dependency. */
        SERVER_HTTP_CONNECTION
    }

    /*
     * ==================== ManagedObjectSource ==================
     */

    @Override
    protected void loadSpecification(SpecificationContext context) {
        // No specification
    }

    @Override
    protected void loadMetaData(MetaDataContext<DependencyKeys, None> context) throws Exception {
        context.setObjectClass(HttpExternalResponse.class);
        context.setManagedObjectClass(HttpExternalResponseManagedObject.class);
        context.addDependency(DependencyKeys.SERVER_HTTP_CONNECTION, ServerHttpConnection.class);
    }

    @Override
    protected ManagedObject getManagedObject() throws Throwable {
        return new HttpExternalResponseManagedObject();
    }

    /**
     * {@link ManagedObject} for the {@link HttpExternalResponse}.
     */
    public static class HttpExternalResponseManagedObject implements CoordinatingManagedObject<DependencyKeys> {

        private ServerHttpConnection connection;

        /*
         * =================== ManagedObject ===================
         */

        @Override
        public void loadObjects(ObjectRegistry<DependencyKeys> registry) throws Throwable {
            this.connection = (ServerHttpConnection) registry.getObject(DependencyKeys.SERVER_HTTP_CONNECTION);
        }

        @Override
        public Object getObject() throws Throwable {
            HttpExternalResponse externalResponse = (HttpExternalResponse) this.connection.getResponse();
            return externalResponse;
        }
    }

}
