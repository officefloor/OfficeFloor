/*-
 * #%L
 * Spring Integration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.spring;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Mock Spring Boot configuration.
 * 
 * @author Daniel Sagenschneider
 */
@SpringBootApplication
public class MockSpringBootConfiguration {

	@Bean
	public QualifiedBean qualifiedOne() {
		return new QualifiedBean("One");
	}

	@Bean("qualifiedTwo")
	public QualifiedBean createTwo() {
		return new QualifiedBean("Two");
	}

	@Bean
	public OfficeFloorInterfaceDependency officeFloorInterfaceDependency() {
		return SpringSupplierSource.getManagedObject("QUALIFIED", OfficeFloorInterfaceDependency.class);
	}

	@Bean
	public OfficeFloorObjectDependency officeFloorObjectDependency() {
		return SpringSupplierSource.getManagedObject(null, OfficeFloorObjectDependency.class);
	}
}
