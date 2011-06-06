/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import java.util.List;

import javax.sql.DataSource;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.gwt.service.GwtServiceWorkSource;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtensionContext;

/**
 * Tests configuring the handling of a GWT service.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtServiceHandleConfigurationTest extends OfficeFrameTestCase {

	/**
	 * Template URI.
	 */
	private static final String TEMPLATE_URI = "template";

	/**
	 * {@link GwtHttpTemplateSectionExtension} to be tested.
	 */
	private final GwtHttpTemplateSectionExtension extension = new GwtHttpTemplateSectionExtension();

	/**
	 * Mock {@link HttpTemplateSectionExtensionContext}.
	 */
	private final HttpTemplateSectionExtensionContext context = this
			.createMock(HttpTemplateSectionExtensionContext.class);

	/**
	 * {@link SectionDesigner}.
	 */
	private final SectionDesigner designer = this
			.createMock(SectionDesigner.class);

	/**
	 * Template content.
	 */
	private String templateContent;

	@Override
	protected void setUp() throws Exception {
		this.templateContent = this.getFileContents(this.findFile(
				this.getClass(), "GwtTemplate.html"));
	}

	/**
	 * Ensure handle no GWT Service.
	 */
	public void testNoGwtService() throws Exception {

		// Record no GWT Service
		this.recordInit(null);

		// Test
		this.replayMockObjects();
		this.extension.extendTemplate(this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure handle GWT Service.
	 */
	public void testGwtService() throws Exception {

		final SectionSourceContext sectionContext = this
				.createMock(SectionSourceContext.class);
		final SectionWork work = this.createMock(SectionWork.class);
		final SectionTask task = this.createMock(SectionTask.class);
		final TaskObject taskObject = this.createMock(TaskObject.class);
		final SectionObject rpcObject = this.createMock(SectionObject.class);
		final SectionInput gwtInput = this.createMock(SectionInput.class);
		final TaskFlow flowOne = this.createMock(TaskFlow.class);
		final SectionTask taskOne = this.createMock(SectionTask.class);
		final TaskFlow flowTwo = this.createMock(TaskFlow.class);
		final SectionTask taskTwo = this.createMock(SectionTask.class);

		final PropertyList properties = OfficeFloorCompiler.newPropertyList();

		// Record template
		this.recordInit(GwtServiceInterfaceAsync.class.getName());

		// Record obtaining details to load GWT service
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		this.recordReturn(this.context, this.context.getSectionSourceContext(),
				sectionContext);
		this.recordReturn(sectionContext, sectionContext.getClassLoader(),
				classLoader);
		this.recordReturn(this.context, this.context.getSectionDesigner(),
				this.designer);
		this.recordReturn(sectionContext, sectionContext.createPropertyList(),
				properties);

		// Record GWT service
		this.recordReturn(this.designer, this.designer.addSectionWork(
				"GWT_GwtServicePath", GwtServiceWorkSource.class.getName()),
				work);
		work.addProperty(
				GwtServiceWorkSource.PROPERTY_GWT_ASYNC_SERVICE_INTERFACE,
				GwtServiceInterfaceAsync.class.getName());
		this.recordReturn(work,
				work.addSectionTask("GWT_GwtServicePath", "service"), task);
		this.recordReturn(task,
				task.getTaskObject("SERVER_GWT_RPC_CONNECTION"), taskObject);
		this.recordReturn(this.context, this.context
				.getOrCreateSectionObject(ServerGwtRpcConnection.class
						.getName()), rpcObject);
		this.designer.link(taskObject, rpcObject);

		// Link input for GWT Service
		this.recordReturn(this.designer,
				this.designer.addSectionInput("GWT_GwtServicePath", null),
				gwtInput);
		this.designer.link(gwtInput, task);

		// Provide the work type
		WorkType<?> workType = WorkLoaderUtil.loadWorkType(
				GwtServiceWorkSource.class,
				GwtServiceWorkSource.PROPERTY_GWT_ASYNC_SERVICE_INTERFACE,
				GwtServiceInterfaceAsync.class.getName());
		this.recordReturn(
				sectionContext,
				sectionContext.loadWorkType(
						GwtServiceWorkSource.class.getName(), properties),
				workType);

		// Record flow 'one' for GWT service
		this.recordReturn(task, task.getTaskFlow("one"), flowOne);
		this.recordReturn(this.context, this.context.getTask("one"), taskOne);
		this.designer.link(flowOne, taskOne,
				FlowInstigationStrategyEnum.SEQUENTIAL);

		// Record flow 'two' for GWT service
		this.recordReturn(task, task.getTaskFlow("two"), flowTwo);
		this.recordReturn(this.context, this.context.getTask("two"), taskTwo);
		this.designer.link(flowTwo, taskTwo,
				FlowInstigationStrategyEnum.SEQUENTIAL);

		// Test
		this.replayMockObjects();
		this.extension.extendTemplate(this.context);
		this.verifyMockObjects();
	}

	/**
	 * Initiate recording.
	 * 
	 * @param gwtServiceInterfaces
	 *            GWT Service Interfaces.
	 */
	private void recordInit(String gwtServiceInterfaces) {
		this.recordReturn(this.context, this.context.getTemplateContent(),
				this.templateContent);
		this.recordReturn(
				this.context,
				this.context
						.getProperty(GwtHttpTemplateSectionExtension.PROPERTY_TEMPLATE_URI),
				TEMPLATE_URI);
		this.recordReturn(
				this.context,
				this.context
						.getProperty(
								GwtHttpTemplateSectionExtension.PROPERTY_GWT_ASYNC_SERVICE_INTERFACES,
								null), gwtServiceInterfaces);
	}

	/**
	 * GWT Service interface.
	 */
	@RemoteServiceRelativePath("GwtServicePath")
	public static interface GwtServiceInterface {

		String one();

		List<String> two(DataSource dataSource);
	}

	/**
	 * GWT Async Service interface.
	 */
	public static interface GwtServiceInterfaceAsync {

		void one(AsyncCallback<String> callback);

		void two(DataSource dataSource, AsyncCallback<List<String>> callback);
	}

}