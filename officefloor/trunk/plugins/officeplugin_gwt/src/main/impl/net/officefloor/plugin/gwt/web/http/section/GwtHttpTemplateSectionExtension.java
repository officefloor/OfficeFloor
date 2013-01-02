/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.gwt.web.http.section;

import net.officefloor.autowire.AutoWire;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.work.TaskFlowType;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.model.gwt.module.GwtModuleModel;
import net.officefloor.plugin.gwt.service.GwtServiceTask.Dependencies;
import net.officefloor.plugin.gwt.service.GwtServiceWorkSource;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnectionManagedObjectSource;
import net.officefloor.plugin.gwt.template.tranform.HtmlTagType;
import net.officefloor.plugin.gwt.template.tranform.HtmlTemplateTransformation;
import net.officefloor.plugin.gwt.template.tranform.HtmlTemplateTransformationContext;
import net.officefloor.plugin.gwt.template.tranform.HtmlTemplateTransformer;
import net.officefloor.plugin.gwt.template.transform.HtmlTemplateTransformerImpl;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSectionExtension;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtensionContext;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * {@link HttpTemplateSectionExtension} to include GWT.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtHttpTemplateSectionExtension implements
		HttpTemplateSectionExtension {

	/**
	 * Obtains the {@link GwtModuleModel} name from the template URI.
	 * 
	 * @param templateUri
	 *            template URI.
	 * @return {@link GwtModuleModel} name.
	 */
	public static String getGwtModuleName(String templateUri) {

		// Determine the module name
		String moduleName;
		if ("/".equals(templateUri)) {
			// default '/' to root
			moduleName = "root";

		} else if (templateUri.startsWith("/")) {
			// Remove leading '/' to make relative path
			moduleName = templateUri.substring("/".length());

		} else {
			// Use template URI as is
			moduleName = templateUri;
		}

		// return the module name
		return moduleName;
	}

	/**
	 * Name of property specifying the URI for the template.
	 */
	public static final String PROPERTY_TEMPLATE_URI = "template.uri";

	/**
	 * Name of property specifying the GWT Async service interfaces.
	 */
	public static final String PROPERTY_GWT_ASYNC_SERVICE_INTERFACES = "gwt.async.service.interfaces";

	/**
	 * Id for the GWT History IFrame.
	 */
	private static final String GWT_HISTORY_ID = "__gwt_historyFrame";

	/**
	 * GWT History IFrame.
	 */
	private static final String GWT_HISTORY_IFRAME = "<iframe src=\"javascript:''\" id=\""
			+ GWT_HISTORY_ID
			+ "\" tabIndex='-1' style=\"position:absolute;width:0;height:0;border:0\"></iframe>";

	/**
	 * Initiates the extending of the template with GWT.
	 * 
	 * @param template
	 *            {@link HttpTemplateAutoWireSection}.
	 * @param application
	 *            {@link WebAutoWireApplication}.
	 * @param properties
	 *            {@link SourceProperties}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @throws Exception
	 *             If fails to extend the template configuration.
	 */
	public static void extendTemplate(
			final HttpTemplateAutoWireSection template,
			final WebAutoWireApplication application,
			SourceProperties properties, ClassLoader classLoader)
			throws Exception {

		// Obtain the template URI
		final String templateUri = template.getTemplateUri();
		if ((templateUri == null) || (templateUri.trim().length() == 0)) {
			throw new IllegalStateException(
					"Template must have a URI for extending with GWT (Template="
							+ template.getTemplatePath() + ")");
		}

		// Configure the Server GWT RPC Connection (only once)
		if (!(application.isObjectAvailable(new AutoWire(
				ServerGwtRpcConnection.class)))) {
			application.addManagedObject(
					ServerGwtRpcConnectionManagedObjectSource.class.getName(),
					null, new AutoWire(ServerGwtRpcConnection.class),
					new AutoWire(AsyncCallback.class));
		}

		// Configure this extension
		HttpTemplateAutoWireSectionExtension extension = template
				.addTemplateExtension(GwtHttpTemplateSectionExtension.class);
		extension.addProperty(PROPERTY_TEMPLATE_URI, templateUri);

		// Determine if GWT Services
		String gwtServiceInterfaceNames = properties.getProperty(
				PROPERTY_GWT_ASYNC_SERVICE_INTERFACES, null);
		if (gwtServiceInterfaceNames != null) {

			// Include GWT Services for configuring servicing
			extension.addProperty(PROPERTY_GWT_ASYNC_SERVICE_INTERFACES,
					gwtServiceInterfaceNames);

			// Configure the GWT Services
			configureGwtServiceInterfaces(gwtServiceInterfaceNames,
					classLoader, new GwtServiceConfigurer() {
						@Override
						public void configure(Class<?> gwtServiceInterface,
								String relativePath, String gwtServiceName) {

							// Obtain the module name
							String moduleName = GwtHttpTemplateSectionExtension
									.getGwtModuleName(templateUri);

							// Route GWT Service
							String uri = moduleName + "/" + relativePath;
							application.linkUri(uri, template, gwtServiceName);
						}
					});
		}
	}

	/**
	 * Obtains the GWT Async Service Interface names from the property value.
	 * 
	 * @param gwtAsyncServiceInterfaceNamesPropertyValue
	 *            GWT Async Service Interface name property value. May be
	 *            <code>null</code>.
	 * @return GWT Async Service Interface names.
	 */
	public static String[] getGwtAsyncServiceInterfaceNames(
			String gwtAsyncServiceInterfaceNamesPropertyValue) {

		// Ensure have value
		if ((gwtAsyncServiceInterfaceNamesPropertyValue == null)
				|| (gwtAsyncServiceInterfaceNamesPropertyValue.trim().length() == 0)) {
			return new String[0]; // no interfaces
		}

		// Obtain the values
		String[] interfaceNames = gwtAsyncServiceInterfaceNamesPropertyValue
				.split(",");
		for (int i = 0; i < interfaceNames.length; i++) {
			interfaceNames[i] = interfaceNames[i].trim();
		}

		// Return the interface names
		return interfaceNames;
	}

	/**
	 * Configures the GWT services.
	 * 
	 * @param gwtServiceInterfaceNames
	 *            GWT Service interface names.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param configurer
	 *            {@link GwtServiceConfigurer}.
	 * @throws Exception
	 *             If fails to configure the GWT services.
	 */
	private static void configureGwtServiceInterfaces(
			String gwtServiceInterfaceNames, ClassLoader classLoader,
			GwtServiceConfigurer configurer) throws Exception {

		// Configure the GWT Service Interfaces
		for (String gwtServiceInterfaceName : getGwtAsyncServiceInterfaceNames(gwtServiceInterfaceNames)) {

			// Load the GWT Service Async interface
			gwtServiceInterfaceName = gwtServiceInterfaceName.trim();
			Class<?> gwtServiceInterface = classLoader
					.loadClass(gwtServiceInterfaceName);

			// Obtain the definition interface name
			String gwtDefinitionInterfaceName = gwtServiceInterfaceName
					.substring(0, (gwtServiceInterfaceName.length() - "Async"
							.length()));

			// Obtain the GWT Service Relative Path
			RemoteServiceRelativePath relativePathAnnotation = gwtServiceInterface
					.getAnnotation(RemoteServiceRelativePath.class);
			if (relativePathAnnotation == null) {
				// Obtain from GWT definition Interface
				Class<?> gwtDefinitionInterface = classLoader
						.loadClass(gwtDefinitionInterfaceName);
				relativePathAnnotation = gwtDefinitionInterface
						.getAnnotation(RemoteServiceRelativePath.class);
			}
			String relativePath = (relativePathAnnotation == null ? null
					: relativePathAnnotation.value());
			if ((relativePath == null) || (relativePath.trim().length() == 0)) {
				throw new IllegalStateException("GWT Service Interface "
						+ gwtDefinitionInterfaceName
						+ " is not annotated with "
						+ RemoteServiceRelativePath.class.getSimpleName());
			}

			// Obtain the GWT Service name
			String gwtServiceName = "GWT_" + relativePath;

			// Configure the GWT Service
			configurer.configure(gwtServiceInterface, relativePath,
					gwtServiceName);
		}
	}

	/**
	 * Configures the GWT Service.
	 */
	private static interface GwtServiceConfigurer {

		/**
		 * Configures the GWT Service.
		 * 
		 * @param gwtServiceInterface
		 *            GWT Service interface.
		 * @param relativePath
		 *            GWT relative path.
		 * @param gwtServiceName
		 *            GWT service name.
		 * @throws Exception
		 *             If fails to configure the GWT Service.
		 */
		void configure(Class<?> gwtServiceInterface, String relativePath,
				String gwtServiceName) throws Exception;
	}

	/*
	 * ================== HttpTemplateSectionExtension ====================
	 */

	@Override
	public void extendTemplate(final HttpTemplateSectionExtensionContext context)
			throws Exception {

		// Obtain the template
		String template = context.getTemplateContent();

		// Construct GWT script
		String templateUri = context.getProperty(PROPERTY_TEMPLATE_URI);
		String moduleName = GwtHttpTemplateSectionExtension
				.getGwtModuleName(templateUri);
		String scriptPath = moduleName + "/" + moduleName + ".nocache.js";
		String script = "<script type=\"text/javascript\" language=\"javascript\" src=\""
				+ scriptPath + "\"></script>";

		// Create the transformer
		HtmlTemplateTransformer transformer = new HtmlTemplateTransformerImpl();

		// Check whether GWT content already included
		Transformation transformation = new Transformation(scriptPath, script);
		transformer.transform(template, transformation);

		// Determine if require adding GWT content
		if ((!transformation.isGwtScriptIncluded)
				|| (!transformation.isGwtHistoryIncluded)) {

			// Ensure have HTML element to include the GWT content
			if (!transformation.isHtmlIncluded) {
				throw new IllegalStateException(
						"Template must include HTML element");
			}

			// Check complete, now undertake transformation
			transformation.isCheck = false;
			template = transformer.transform(template, transformation);

			// Write back the template
			context.setTemplateContent(template);
		}

		// Obtain the GWT Service Interfaces
		String gwtServiceInterfaceNames = context.getProperty(
				PROPERTY_GWT_ASYNC_SERVICE_INTERFACES, null);
		if ((gwtServiceInterfaceNames != null)
				&& (gwtServiceInterfaceNames.trim().length() > 0)) {

			// Obtain the Class Loader
			final SectionSourceContext sectionContext = context
					.getSectionSourceContext();
			ClassLoader classLoader = sectionContext.getClassLoader();

			// Obtain the Section Designer
			final SectionDesigner designer = context.getSectionDesigner();

			// Configure the GWT Services
			configureGwtServiceInterfaces(gwtServiceInterfaceNames,
					classLoader, new GwtServiceConfigurer() {
						@Override
						public void configure(Class<?> gwtServiceInterface,
								String relativePath, String gwtServiceName)
								throws Exception {

							// Create properties for the GWT Service Work
							PropertyList properties = sectionContext
									.createPropertyList();
							properties
									.addProperty(
											GwtServiceWorkSource.PROPERTY_GWT_ASYNC_SERVICE_INTERFACE)
									.setValue(gwtServiceInterface.getName());

							// Add the GWT Service task
							SectionWork work = designer.addSectionWork(
									gwtServiceName,
									GwtServiceWorkSource.class.getName());
							for (Property property : properties) {
								work.addProperty(property.getName(),
										property.getValue());
							}
							SectionTask task = work.addSectionTask(
									gwtServiceName,
									GwtServiceWorkSource.SERVICE_TASK_NAME);

							// Add GWT RPC connection dependency
							TaskObject connectionDependency = task
									.getTaskObject(Dependencies.SERVER_GWT_RPC_CONNECTION
											.name());
							SectionObject connectionObject = context
									.getOrCreateSectionObject(ServerGwtRpcConnection.class
											.getName());
							designer.link(connectionDependency,
									connectionObject);

							// Link input for task
							SectionInput serviceInput = designer
									.addSectionInput(gwtServiceName, null);
							designer.link(serviceInput, task);

							// Link flows
							WorkType<?> workType = sectionContext.loadWorkType(
									GwtServiceWorkSource.class.getName(),
									properties);
							if (workType != null) {
								TaskType<?, ?, ?> taskType = workType
										.getTaskTypes()[0];
								for (TaskFlowType<?> flowType : taskType
										.getFlowTypes()) {

									// Should always have flow
									String methodName = flowType.getFlowName();
									TaskFlow flow = task
											.getTaskFlow(methodName);

									// Obtain the task for flow
									SectionTask flowTask = context
											.getTask(methodName);
									if (flowTask == null) {
										throw new IllegalStateException(
												"No service implementation for GWT service method '"
														+ gwtServiceInterface
																.getSimpleName()
														+ "."
														+ methodName
														+ "(...)' on template logic class "
														+ context
																.getTemplateClass()
																.getName());
									}

									// GWT service method not render template
									context.flagAsNonRenderTemplateMethod(methodName);

									// Link handling of GWT method
									designer.link(
											flow,
											flowTask,
											FlowInstigationStrategyEnum.SEQUENTIAL);
								}
							}
						}
					});
		}
	}

	/**
	 * Checks the template for content.
	 */
	private static class Transformation implements HtmlTemplateTransformation {

		/**
		 * GWT script path.
		 */
		private final String scriptPath;

		/**
		 * GWT script content.
		 */
		private final String script;

		/**
		 * Determining if checking.
		 */
		public boolean isCheck = true;

		/**
		 * Flag indicating if HTML included.
		 */
		private boolean isHtmlIncluded = false;

		/**
		 * Flag indicating if HEAD included.
		 */
		private boolean isHeadIncluded = false;

		/**
		 * Flag indicating if the GWT script is included.
		 */
		public boolean isGwtScriptIncluded = false;

		/**
		 * Flag indicating if BODY included.
		 */
		private boolean isBodyIncluded = false;

		/**
		 * Flag indicating if the GWT history is included.
		 */
		public boolean isGwtHistoryIncluded = false;

		/**
		 * Initiate.
		 * 
		 * @param scriptPath
		 *            GWT script path.
		 * @param script
		 *            GWT script content.
		 */
		public Transformation(String scriptPath, String script) {
			this.scriptPath = scriptPath;
			this.script = script;
		}

		/*
		 * =============== HtmlTemplateTransformation ====================
		 */

		@Override
		public void transform(HtmlTemplateTransformationContext context) {

			// Determine if checking
			if (this.isCheck) {
				// Checking content

				// Determine if HTML element
				if ("html".equalsIgnoreCase(context.getTagName())) {
					this.isHtmlIncluded = true;
				}

				// Determine if HEAD element
				if ("head".equalsIgnoreCase(context.getTagName())) {
					this.isHeadIncluded = true;
				}

				// Determine if BODY element
				if ("body".equalsIgnoreCase(context.getTagName())) {
					this.isBodyIncluded = true;
				}

				// Determine if GWT script
				if ("script".equalsIgnoreCase(context.getTagName())
						&& (this.scriptPath.equals(context
								.getAttributeValue("src")))) {
					// GWT script included
					this.isGwtScriptIncluded = true;
				}

				// Determine if GWT iframe
				if ("iframe".equalsIgnoreCase(context.getTagName())
						&& (GWT_HISTORY_ID.equals(context
								.getAttributeValue("id")))) {
					// GWT history included
					this.isGwtHistoryIncluded = true;
				}

			} else {
				// Transform the content

				// Determine if GWT script
				if (!this.isGwtScriptIncluded) {
					// Include GWT script
					boolean isInclude = false;
					String prefix = "";
					String suffix = "";
					if ((!this.isHeadIncluded)
							&& ("html".equalsIgnoreCase(context.getTagName()))) {
						isInclude = true;
						prefix = "<head>";
						suffix = "</head>";
					}
					if ("head".equalsIgnoreCase(context.getTagName())) {
						isInclude = true;
					}

					// Include GWT if appropriate
					if (isInclude) {
						context.inputContent(prefix + this.script + suffix);
						this.isGwtScriptIncluded = true;
					}
				}

				// Determine if GWT history
				if (!this.isGwtHistoryIncluded) {
					// Include GWT history
					boolean isInclude = false;
					boolean isAppend = false;
					String prefix = "";
					String suffix = "";
					if (!this.isBodyIncluded) {
						prefix = "<body>";
						suffix = "</body>";
						if (!this.isHeadIncluded) {
							// No head so include in HTML
							if ("html".equalsIgnoreCase(context.getTagName())) {
								isInclude = true;
							}
						} else {
							// Append after the HEAD
							if ("head".equalsIgnoreCase(context.getTagName())) {
								HtmlTagType tagType = context.getTagType();
								if ((HtmlTagType.OPEN_CLOSE.equals(tagType))
										|| (HtmlTagType.CLOSE.equals(tagType))) {
									isAppend = true;
								}
							}
						}
					}
					if ("body".equalsIgnoreCase(context.getTagName())) {
						isInclude = true;
					}

					// Include GWT if appropriate
					if (isInclude) {
						context.inputContent(prefix + GWT_HISTORY_IFRAME
								+ suffix);
						this.isGwtHistoryIncluded = true;
					} else if (isAppend) {
						context.appendContent(prefix + GWT_HISTORY_IFRAME
								+ suffix);
						this.isGwtHistoryIncluded = true;
					}
				}
			}
		}
	}

}