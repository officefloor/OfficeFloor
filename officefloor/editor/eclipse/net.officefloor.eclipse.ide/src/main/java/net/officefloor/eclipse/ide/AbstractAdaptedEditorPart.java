/*-
 * #%L
 * [bundle] OfficeFloor Eclipse IDE
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.eclipse.ide;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.function.Function;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.mvc.fx.domain.IDomain;
import org.eclipse.gef.mvc.fx.ui.parts.AbstractFXEditor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import javafx.scene.Scene;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.eclipse.bridge.EclipseEnvironmentBridge;
import net.officefloor.eclipse.bridge.ProjectConfigurationContext;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor.ViewManager;
import net.officefloor.model.Model;

/**
 * {@link EditorPart} for the {@link AbstractAdaptedIdeEditor}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractAdaptedEditorPart<R extends Model, RE extends Enum<RE>, O> extends AbstractFXEditor {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAdaptedEditorPart.class);

	/**
	 * Initialises the {@link AbstractAdaptedIdeEditor}.
	 * 
	 * @param editor      {@link AbstractAdaptedIdeEditor} to initialise.
	 * @param initialiser Initialiser of the {@link AbstractAdaptedIdeEditor}.
	 */
	public static void initEditor(AbstractAdaptedIdeEditor<?, ?, ?> editor, Function<Injector, IDomain> initialiser) {
		editor.init(new AdaptedMvcFxUiModule(), initialiser);
	}

	/**
	 * {@link Injector} that does nothing, so can use {@link AbstractFXEditor} as
	 * super {@link Class} (requirement of constructor).
	 */
	private static final Injector doNothingInjector;

	static {
		try {
			doNothingInjector = (Injector) Proxy.newProxyInstance(AbstractAdaptedEditorPart.class.getClassLoader(),
					new Class[] { Injector.class }, (proxy, method, args) -> null);
		} catch (Exception ex) {
			// Invalid, as should create proxy
			throw new IllegalStateException("Unable to create Injector mock for loading class", ex);
		}
	}

	/**
	 * {@link EclipseEnvironmentBridge}.
	 */
	private EclipseEnvironmentBridge envBridge;

	/**
	 * {@link AbstractAdaptedIdeEditor}.
	 */
	private AbstractAdaptedIdeEditor<R, RE, O> editor;

	/**
	 * {@link ViewManager}.
	 */
	private ViewManager<R> viewManager;

	/**
	 * Creates the {@link AbstractAdaptedIdeEditor}.
	 * 
	 * @param envBridge {@link EnvironmentBridge}.
	 * @return {@link AbstractAdaptedIdeEditor}.
	 */
	public abstract AbstractAdaptedIdeEditor<R, RE, O> createEditor(EnvironmentBridge envBridge);

	/**
	 * Instantiate.
	 */
	public AbstractAdaptedEditorPart() {
		super(doNothingInjector);
	}

	/*
	 * ====================== EditorPart ============================
	 */

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {

		// Ensure only file input
		if (!(input instanceof IFileEditorInput)) {
			throw new IllegalStateException("Unable to edit input " + input.getClass().getName());
		}

		// Load the configuration item from the file
		IFileEditorInput fileInput = (IFileEditorInput) input;
		IFile configurationFile = fileInput.getFile();
		WritableConfigurationItem configurationItem = ProjectConfigurationContext
				.getWritableConfigurationItem(configurationFile, null);

		// Obtain the file (and subsequently it's project)
		IFile file = fileInput.getFile();
		IProject project = file.getProject();

		// Obtain the java project
		IJavaProject javaProject = JavaCore.create(project);

		// Create the Eclipse environment
		this.envBridge = new EclipseEnvironmentBridge(javaProject);

		// Create and initialise the editor
		this.editor = this.createEditor(this.envBridge);
		initEditor(this.editor, (injector) -> {
			injector.injectMembers(this);
			return this.getDomain();
		});
		this.editor.setConfigurationItem(configurationItem);

		// Initialise parent
		super.init(site, input);
	}

	@Override
	public void createPartControl(Composite parent) {

		// Obtain the parent shell
		Shell parentShell = parent.getShell();
		this.envBridge.init(parentShell);

		// Create the visual
		super.createPartControl(parent);
	}

	@Override
	protected void hookViewers() {

		// Load the view
		this.viewManager = this.editor.loadView((view) -> {
			this.getCanvas().setScene(new Scene(view));
		});
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		this.viewManager.save();
		this.markNonDirty();
	}

	@Override
	public void doSaveAs() {
		// TODO implement EditorPart.doSaveAs
		throw new UnsupportedOperationException("TODO implement EditorPart.doSaveAs");
	}

	@Override
	public void dispose() {
		try {
			this.envBridge.dispose();
		} catch (IOException ex) {
			LOGGER.warn("Failed to dispose of " + EclipseEnvironmentBridge.class.getSimpleName(), ex);
		}
		super.dispose();
	}

}
