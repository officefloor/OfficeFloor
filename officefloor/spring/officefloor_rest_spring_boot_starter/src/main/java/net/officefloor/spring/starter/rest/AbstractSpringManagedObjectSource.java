package net.officefloor.spring.starter.rest;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.server.http.ServerHttpConnection;

import java.util.function.Function;

/**
 * Abstract {@link net.officefloor.frame.api.managedobject.source.ManagedObjectSource} from the {@link SpringServerHttpConnection}.
 */
public class AbstractSpringManagedObjectSource<S> extends AbstractManagedObjectSource<AbstractSpringManagedObjectSource.DependencyKeys, None> {

    /**
     * Dependency keys for {@link SpringServerHttpConnection}.
     */
    public static enum DependencyKeys {
        SERVER_HTTP_CONNECTION
    }

    /**
     * Object type.
     */
    private final Class<S> objectType;

    /**
     * Extracts the object from the {@link SpringServerHttpConnection}.
     */
    private final Function<SpringServerHttpConnection, S> extractObject;

    /**
     * Instantiate.
     *
     * @param objectType    Object type.
     * @param extractObject Extracts the object from the {@link SpringServerHttpConnection}.
     */
    public AbstractSpringManagedObjectSource(Class<S> objectType, Function<SpringServerHttpConnection, S> extractObject) {
        this.objectType = objectType;
        this.extractObject = extractObject;
    }

    /*
     * ==================== ManagedObjectSource ===================
     */

    @Override
    protected void loadSpecification(SpecificationContext context) {
        // No specification
    }

    @Override
    protected void loadMetaData(MetaDataContext<DependencyKeys, None> context) throws Exception {
        context.setObjectClass(this.objectType);
        context.setManagedObjectClass(SpringManagedObject.class);
        context.addDependency(DependencyKeys.SERVER_HTTP_CONNECTION, ServerHttpConnection.class);
    }

    @Override
    protected ManagedObject getManagedObject() throws Throwable {
        return new SpringManagedObject();
    }

    /**
     * {@link ManagedObject} to extract object from the {@link SpringServerHttpConnection}.
     */
    private class SpringManagedObject implements CoordinatingManagedObject<DependencyKeys> {

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
            return AbstractSpringManagedObjectSource.this.extractObject.apply(this.connection);
        }
    }

}
