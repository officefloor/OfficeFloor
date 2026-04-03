package net.officefloor.spring.starter.rest.servlet;

import jakarta.servlet.http.HttpServletResponse;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.server.http.HttpExternalResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.spring.starter.rest.SpringServerHttpConnection;

/**
 * {@link net.officefloor.frame.api.managedobject.source.ManagedObjectSource} for the {@link HttpServletResponse}.
 */
public class HttpServletResponseManagedObjectSource extends AbstractManagedObjectSource<HttpServletResponseManagedObjectSource.DependencyKeys, None> {

    /**
     * Dependency keys.
     */
    public static enum DependencyKeys {
        SERVER_HTTP_CONNECTION,
        HTTP_EXTERNAL_RESPONSE
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
        context.setObjectClass(HttpServletResponse.class);
        context.setManagedObjectClass(HttpServletResponseManagedObject.class);
        context.addDependency(DependencyKeys.SERVER_HTTP_CONNECTION, ServerHttpConnection.class);
        context.addDependency(DependencyKeys.HTTP_EXTERNAL_RESPONSE, HttpExternalResponse.class);
    }

    @Override
    protected ManagedObject getManagedObject() throws Throwable {
        return new HttpServletResponseManagedObject();
    }

    /**
     * {@link ManagedObject} to extract object from the {@link HttpServletResponse}.
     */
    private static class HttpServletResponseManagedObject implements CoordinatingManagedObject<DependencyKeys> {

        /**
         * {@link SpringServerHttpConnection}.
         */
        private SpringServerHttpConnection connection;

        /**
         * {@link HttpExternalResponse}.
         */
        private HttpExternalResponse externalResponse;

        /*
         * =============== ManagedObject ================
         */

        @Override
        public void loadObjects(ObjectRegistry<DependencyKeys> registry) throws Throwable {
            this.connection = (SpringServerHttpConnection) registry.getObject(DependencyKeys.SERVER_HTTP_CONNECTION);
            this.externalResponse = (HttpExternalResponse) registry.getObject(DependencyKeys.HTTP_EXTERNAL_RESPONSE);
        }

        @Override
        public Object getObject() throws Throwable {

            // Using the HTTP Servlet Response, so external send
            this.externalResponse.externalSend();

            // Provide Http Servlet Response
            return this.connection.getHttpServletResponse();
        }
    }

}
