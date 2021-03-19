/*-
 * #%L
 * Spring Integration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
