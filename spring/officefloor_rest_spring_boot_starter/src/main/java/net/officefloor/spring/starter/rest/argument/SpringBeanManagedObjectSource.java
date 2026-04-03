package net.officefloor.spring.starter.rest.argument;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource;
import org.springframework.beans.factory.BeanFactory;

/**
 * {@link net.officefloor.frame.api.managedobject.source.ManagedObjectSource} for a Spring Bean.
 */
public class SpringBeanManagedObjectSource extends AbstractAsyncManagedObjectSource<None, None> implements ManagedObject {

    private final String beanName;

    private final Class<?> objectType;

    private final BeanFactory beanFactory;

    public SpringBeanManagedObjectSource(String beanName, Class<?> objectType, BeanFactory beanFactory) {
        this.beanName = beanName;
        this.objectType = objectType;
        this.beanFactory = beanFactory;
    }

    /*
     * ===================== ManagedObjectSource ===================
     */

    @Override
    protected void loadSpecification(SpecificationContext context) {
        // No specification
    }

    @Override
    protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
        context.setObjectClass(this.objectType);
        context.setManagedObjectClass(this.getClass());
    }

    @Override
    public void sourceManagedObject(ManagedObjectUser user) {
        user.setManagedObject(this);
    }

    /*
     * ========================= ManagedObject =======================
     */

    @Override
    public Object getObject() throws Throwable {
        return this.beanFactory.getBean(this.beanName);
    }

}
