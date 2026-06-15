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

package net.officefloor.spring.starter.rest.view;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpExternalResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.spring.starter.rest.ModelAndViewBridge;
import net.officefloor.spring.starter.rest.SpringServerHttpConnection;
import org.springframework.web.servlet.ModelAndView;

/**
 * {@link net.officefloor.frame.api.managedobject.source.ManagedObjectSource} for the {@link ViewResponse}.
 */
public class ViewResponseManagedObjectSource extends AbstractManagedObjectSource<ViewResponseManagedObjectSource.DependencyKeys, None> {

    /** Dependency keys. */
    public static enum DependencyKeys {
        /** Server HTTP connection. */
        SERVER_HTTP_CONNECTION
    }

    /*
     * ===================== ManagedObjectSource ==================
     */

    @Override
    protected void loadSpecification(SpecificationContext context) {
        // No specification
    }

    @Override
    protected void loadMetaData(MetaDataContext<DependencyKeys, None> context) throws Exception {
        context.setObjectClass(ViewResponse.class);
        context.setManagedObjectClass(ViewResponseManagedObject.class);
        context.addDependency(DependencyKeys.SERVER_HTTP_CONNECTION, ServerHttpConnection.class);
    }

    @Override
    protected ManagedObject getManagedObject() throws Throwable {
        return new ViewResponseManagedObject();
    }

    /**
     * {@link ManagedObject} for the {@link ViewResponse}.
     */
    private static class ViewResponseManagedObject implements CoordinatingManagedObject<DependencyKeys> {

        private SpringServerHttpConnection connection;

        /*
         * ===================== ManagedObject ===================
         */

        @Override
        public void loadObjects(ObjectRegistry<DependencyKeys> registry) throws Throwable {
            this.connection = (SpringServerHttpConnection) registry.getObject(DependencyKeys.SERVER_HTTP_CONNECTION);
        }

        @Override
        public Object getObject() throws Throwable {

            // Return the view response
            return new ViewResponse() {
                @Override
                public void send(String view) {

                    // Send view response
                    ViewResponseManagedObject.this.connection.setResponse(null, null, view, null);
                }
            };
        }
    }

}
