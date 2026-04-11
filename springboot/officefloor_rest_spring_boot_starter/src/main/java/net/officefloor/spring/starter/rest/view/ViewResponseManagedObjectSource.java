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

    public static enum DependencyKeys {
        SERVER_HTTP_CONNECTION,
        HTTP_EXTERNAL_RESPONSE
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
        context.addDependency(DependencyKeys.HTTP_EXTERNAL_RESPONSE, HttpExternalResponse.class);
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

        private HttpExternalResponse externalResponse;

        /*
         * ===================== ManagedObject ===================
         */

        @Override
        public void loadObjects(ObjectRegistry<DependencyKeys> registry) throws Throwable {
            this.connection = (SpringServerHttpConnection) registry.getObject(DependencyKeys.SERVER_HTTP_CONNECTION);
            this.externalResponse = (HttpExternalResponse) registry.getObject(DependencyKeys.HTTP_EXTERNAL_RESPONSE);
        }

        @Override
        public Object getObject() throws Throwable {

            // Obtain the bridge to render result
            ModelAndViewBridge renderBridge = this.connection.getRenderModelAndViewBridge();

            // Return the view response
            return new ViewResponse() {
                @Override
                public void send(String view) {

                    // Send the response
                    try {
                        ModelAndView modelAndView = renderBridge.getModelAndView(view);
                        renderBridge.processDispatchResult(modelAndView, null);
                    } catch (Exception ex) {
                        throw new HttpException(ex);
                    }

                    // Flag that externally handled
                    ViewResponseManagedObject.this.externalResponse.externalSend();
                }
            };
        }
    }

}
