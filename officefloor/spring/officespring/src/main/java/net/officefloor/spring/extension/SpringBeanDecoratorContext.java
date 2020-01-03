package net.officefloor.spring.extension;

/**
 * Spring Bean decorator context.
 * 
 * @author Daniel Sagenschneider
 */
public interface SpringBeanDecoratorContext {

	/**
	 * Obtains the name of the Spring Bean.
	 * 
	 * @return Name of the Spring Bean.
	 */
	String getBeanName();

	/**
	 * Obtains the type of the Spring Bean.
	 * 
	 * @return Type of the Spring Bean.
	 */
	Class<?> getBeanType();

	/**
	 * Adds a further dependency for the Spring Bean.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code>.
	 * @param type      Type for dependency.
	 */
	void addDependency(String qualifier, Class<?> type);

}