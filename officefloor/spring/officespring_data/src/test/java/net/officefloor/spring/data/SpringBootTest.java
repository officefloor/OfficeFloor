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

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure can integrate Spring via boot.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringBootTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurableApplicationContext}.
	 */
	private ConfigurableApplicationContext context;

	@Override
	protected void setUp() throws Exception {
		this.context = SpringApplication.run(MockSpringBootConfiguration.class);

		// Indicate the registered beans
		System.out.println("Beans:");
		for (String name : this.context.getBeanDefinitionNames()) {
			System.out.println("  " + name);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		this.context.close();
	}

	/**
	 * Ensure can obtain Spring data beans.
	 */
	public void testSpringDataBeans() {

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

}