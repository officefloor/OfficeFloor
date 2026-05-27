package net.officefloor.web.thymeleaf;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * {@link net.officefloor.frame.api.managedobject.source.ManagedObjectSource} providing an application-scoped {@link TemplateEngine}.
 */
public class ThymeleafManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	public static final String PROPERTY_PREFIX = "officefloor.thymeleaf.prefix";

	public static final String PROPERTY_SUFFIX = "officefloor.thymeleaf.suffix";

	public static final String DEFAULT_PREFIX = "templates/";

	public static final String DEFAULT_SUFFIX = ".html";

	private TemplateEngine templateEngine;

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_PREFIX, "Template prefix path");
		context.addProperty(PROPERTY_SUFFIX, "Template file suffix");
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		String prefix = context.getManagedObjectSourceContext().getProperty(PROPERTY_PREFIX, DEFAULT_PREFIX);
		String suffix = context.getManagedObjectSourceContext().getProperty(PROPERTY_SUFFIX, DEFAULT_SUFFIX);

		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
		resolver.setPrefix(prefix);
		resolver.setSuffix(suffix);
		resolver.setTemplateMode(TemplateMode.HTML);
		resolver.setCharacterEncoding("UTF-8");

		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(resolver);

		context.setObjectClass(TemplateEngine.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return () -> this.templateEngine;
	}

}
