package net.officefloor.spring.starter.rest.servlet;

import jakarta.servlet.http.HttpServletRequest;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.spring.starter.rest.SpringServerHttpConnection;

/**
 * {@link net.officefloor.frame.api.managedobject.source.ManagedObjectSource} for the {@link HttpServletRequest}.
 */
public class HttpServletRequestManagedObjectSource extends AbstractManagedObjectSource<HttpServletRequestManagedObjectSource.DependencyKeys, None> {

    /**
     * Dependency keys.
     */
    public static enum DependencyKeys {
        SERVER_HTTP_CONNECTION
    }

    /*
     * ===================== ManagedObjectSource =======================
     */

    @Override
    protected void loadSpecification(SpecificationContext context) {
        // No specification
    }

    @Override
    protected void loadMetaData(MetaDataContext<DependencyKeys, None> context) throws Exception {
        context.setObjectClass(HttpServletRequest.class);
        context.setManagedObjectClass(HttpServletRequestManagedObject.class);
        context.addDependency(DependencyKeys.SERVER_HTTP_CONNECTION, ServerHttpConnection.class);
    }

    @Override
    protected ManagedObject getManagedObject() throws Throwable {
        return new HttpServletRequestManagedObject();
    }

    /**
     * {@link ManagedObject} to extract object from the {@link HttpServletRequest}.
     */
    private static class HttpServletRequestManagedObject implements CoordinatingManagedObject<DependencyKeys> {

        /**
         * {@link SpringServerHttpConnection}.
         */
        private SpringServerHttpConnection connection;

        /*
         * =============== ManagedObject ================
         */

        @Override
        public void loadObjects(ObjectRegistry<DependencyKeys> registry) throws Throwable {
            this.connection = (SpringServerHttpConnection) registry.getObject(DependencyKeys.SERVER_HTTP_CONNECTION);
        }

        @Override
        public Object getObject() throws Throwable {
            return this.connection.getHttpServletRequest();
        }
    }

}
