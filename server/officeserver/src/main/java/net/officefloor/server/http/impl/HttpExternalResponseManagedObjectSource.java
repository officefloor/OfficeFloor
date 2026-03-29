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
