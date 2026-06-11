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

    /**
     * @param beanName    Spring bean name.
     * @param objectType  Object type.
     * @param beanFactory {@link BeanFactory}.
     */
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
        context.addManagedObjectExtension((Class) this.objectType, (managedObject) -> managedObject.getObject());
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
