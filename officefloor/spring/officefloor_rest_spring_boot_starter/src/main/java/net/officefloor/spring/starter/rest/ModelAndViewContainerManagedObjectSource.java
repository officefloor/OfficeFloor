package net.officefloor.spring.starter.rest;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link net.officefloor.frame.api.managedobject.source.ManagedObjectSource} for the {@link ModelAndViewContainer}.
 */
public class ModelAndViewContainerManagedObjectSource extends AbstractManagedObjectSource<None, None> implements ManagedObject {

    /*
     * ================== ManagedObjectSource ===================
     */

    @Override
    protected void loadSpecification(SpecificationContext context) {
        // No specification
    }

    @Override
    protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
        context.setObjectClass(ModelAndViewContainer.class);
    }

    @Override
    protected ManagedObject getManagedObject() throws Throwable {
        return this;
    }

    /*
     * ====================== ManagedObject =====================
     */

    @Override
    public Object getObject() throws Throwable {
        return new ModelAndViewContainer();
    }

}
