/*-
 * #%L
 * Thread Affinity Tutorial
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

package net.officefloor.tutorial.threadaffinityhttpserver;

import javax.persistence.EntityManager;

import lombok.Data;

/**
 * Logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
public class Template {

	@Data
	public static class TemplateData {

		private final String threadName;

		private final Cpu[] cpus;
	}

	public TemplateData getTemplateData(EntityManager entityManager) {

		// Obtain the current thread name
		String threadName = Thread.currentThread().getName();

		// Obtain the CPU instances from data store
		Cpu[] cpus = entityManager.createNamedQuery("AllCpus", Cpu.class).getResultStream().toArray(Cpu[]::new);

		// Return the data
		return new TemplateData(threadName, cpus);
	}

}
