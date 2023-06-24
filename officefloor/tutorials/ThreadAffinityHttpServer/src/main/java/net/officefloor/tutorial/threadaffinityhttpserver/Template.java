package net.officefloor.tutorial.threadaffinityhttpserver;

import jakarta.persistence.EntityManager;
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