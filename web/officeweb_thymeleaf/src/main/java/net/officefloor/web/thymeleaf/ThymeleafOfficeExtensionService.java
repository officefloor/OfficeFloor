package net.officefloor.web.thymeleaf;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * {@link OfficeExtensionService} that auto-registers the {@link ThymeleafManagedObjectSource}
 * so that a {@link org.thymeleaf.TemplateEngine} is available for auto-wiring into
 * {@link ThymeleafProcedureSource} render functions.
 */
public class ThymeleafOfficeExtensionService implements OfficeExtensionService, OfficeExtensionServiceFactory {

	static final String MANAGED_OBJECT_NAME = "_THYMELEAF_ENGINE_";

	/*
	 * ================ OfficeExtensionServiceFactory =================
	 */

	@Override
	public OfficeExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * =================== OfficeExtensionService =====================
	 */

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {
		OfficeManagedObjectSource mos = officeArchitect.addOfficeManagedObjectSource(
				MANAGED_OBJECT_NAME, ThymeleafManagedObjectSource.class.getName());
		mos.addProperty(ThymeleafManagedObjectSource.PROPERTY_PREFIX, ThymeleafManagedObjectSource.DEFAULT_PREFIX);
		mos.addProperty(ThymeleafManagedObjectSource.PROPERTY_SUFFIX, ThymeleafManagedObjectSource.DEFAULT_SUFFIX);
		mos.addOfficeManagedObject(MANAGED_OBJECT_NAME, ManagedObjectScope.PROCESS);
	}

}
