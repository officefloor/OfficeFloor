/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.spring.data;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.spring.SpringSupplierSource;

/**
 * Ensure can integrate Spring via boot.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringDataTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurableApplicationContext}.
	 */
	private ConfigurableApplicationContext context;

	@Override
	protected void setUp() throws Exception {
		this.context = SpringApplication.run(MockSpringDataConfiguration.class, "spring.jmx.default-domain=other");
	}

	@Override
	protected void tearDown() throws Exception {
		this.context.close();
	}

	/**
	 * Ensure can obtain Spring data beans.
	 */
	public void testSpringDataBeans() {

		// Indicate the registered beans
		System.out.println("Beans:");
		for (String name : this.context.getBeanDefinitionNames()) {
			Object bean = this.context.getBean(name);
			System.out.println(
					"  " + name + "\t\t(" + bean.getClass().getName() + ") - " + (bean instanceof RowRepository));
		}

		// Ensure can obtain repository
		RowRepository repository = this.context.getBean(RowRepository.class);
		assertNotNull("Should obtain repository", repository);

		// Add rows
		repository.save(new Row(null, "One"));
		repository.save(new Row(null, "Two"));

		// Ensure can obtain the row
		List<Row> rows = repository.findByName("One");
		assertEquals("Should find a row", 1, rows.size());
	}

	/**
	 * Ensure can run transaction.
	 */
	public void testTransaction() {

		// Obtain the transaction manager
		PlatformTransactionManager transactionManager = this.context.getBean(PlatformTransactionManager.class);
		assertNotNull("Should obtain transaction manager", transactionManager);

		// Undertake transaction
		TransactionStatus transaction = transactionManager.getTransaction(null);

		// Save item within the transaction
		RowRepository repository = this.context.getBean(RowRepository.class);
		repository.save(new Row(null, "One"));

		// Ensure can find row
		assertEquals("Should find row", 1, repository.findByName("One").size());

		// Rollback transaction
		transactionManager.rollback(transaction);

		// Ensure row no longer available
		assertEquals("Should not find row after rollback", 0, repository.findByName("One").size());
	}

	/**
	 * Ensure can use {@link RowRepository}.
	 */
	public void testInjectRepository() throws Throwable {

		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			context.addSection("SECTION", GetRowSection.class);
			SpringSupplierSource.configureSpring(context.getOfficeArchitect(), MockSpringDataConfiguration.class);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Create row
			Row row = new Row(null, "TEST");
			this.context.getBean(RowRepository.class).save(row);

			// Trigger function to use repository within OfficeFloor
			Request request = new Request(row.getId());
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", request);

			// Ensure obtained row
			assertNotNull("Should obtain the row", request.row);
			assertEquals("Incorrect row", row.getId(), request.row.getId());
			assertEquals("Should match name", "TEST", request.row.getName());
		}
	}

	private static class Request {

		private final Long id;

		private volatile Row row;

		private Request(Long id) {
			this.id = id;
		}
	}

	public static class GetRowSection {

		public void service(@Parameter Request request, RowRepository repository) {
			request.row = repository.findById(request.id).get();
		}
	}

}