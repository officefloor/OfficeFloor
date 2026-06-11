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

package net.officefloor.spring.starter.rest.supplier;

import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;

@TestSource
public class AnotherSupplierSource extends AbstractSupplierSource {

    private enum NoKeys {}

    @Override
    protected void loadSpecification(SpecificationContext context) {}

    @Override
    public void supply(SupplierSourceContext context) throws Exception {
        context.addManagedObjectSource(null, AnotherSupplied.class,
                new AbstractManagedObjectSource<NoKeys, NoKeys>() {
                    @Override
                    protected void loadSpecification(SpecificationContext context) {}

                    @Override
                    protected void loadMetaData(MetaDataContext<NoKeys, NoKeys> context) throws Exception {
                        context.setObjectClass(AnotherSupplied.class);
                    }

                    @Override
                    protected ManagedObject getManagedObject() throws Throwable {
                        return AnotherSupplied::new;
                    }
                });
    }

    @Override
    public void terminate() {}
}
