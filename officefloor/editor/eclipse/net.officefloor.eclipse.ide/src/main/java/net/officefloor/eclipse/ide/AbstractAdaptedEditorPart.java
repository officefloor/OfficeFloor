/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.ide;

import java.lang.reflect.Proxy;
import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.mvc.fx.ui.MvcFxUiModule;
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

import com.google.inject.Injector;

import javafx.scene.Scene;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.eclipse.bridge.EclipseEnvironmentBridge;
import net.officefloor.eclipse.bridge.ProjectConfigurationContext;
import net.officefloor.eclipse.ide.preferences.PreferencesEditorInput;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.model.Model;

/**
 * {@link EditorPart} for the {@link AbstractAdaptedIdeEditor}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractAdaptedEditorPart<R extends Model, RE extends Enum<RE>, O> extends AbstractFXEditor {

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
	 * Creates the {@link AbstractAdaptedIdeEditor}.
	 * 
	 * @param <R>       Root {@link Model}.
	 * @param <RE>      Root event {@link Enum}.
	 * @param <O>       Operations.
	 * @param envBridge {@link EnvironmentBridge}.
	 * @return {@link AbstractAdaptedIdeEditor}.
	 */
	protected abstract AbstractAdaptedIdeEditor<R, RE, O> createEditor(EnvironmentBridge envBridge);

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

		// Determine the setup
		EclipseEnvironmentBridge envBridge;
		Consumer<AbstractAdaptedIdeEditor<R, RE, O>> loadModel;

		// Obtain the input configuration
		if (input instanceof IFileEditorInput) {
			// Load the configuration item from the file
			IFileEditorInput fileInput = (IFileEditorInput) input;
			IFile configurationFile = fileInput.getFile();
			WritableConfigurationItem configurationItem = ProjectConfigurationContext
					.getWritableConfigurationItem(configurationFile, null);
			loadModel = (editor) -> this.editor.setConfigurationItem(configurationItem);

			// Obtain the file (and subsequently it's project)
			IFile file = fileInput.getFile();
			IProject project = file.getProject();

			// Obtain the java project
			IJavaProject javaProject = JavaCore.create(project);

			// Create the Eclipse environment
			envBridge = new EclipseEnvironmentBridge(javaProject);

		} else if (input instanceof PreferencesEditorInput) {
			// Provided the model
			PreferencesEditorInput preferencesInput = (PreferencesEditorInput) input;
			@SuppressWarnings("unchecked")
			R model = (R) preferencesInput.getRootModel();
			loadModel = (editor) -> this.editor.setModel(model);

			// No project
			envBridge = new EclipseEnvironmentBridge(null);

		} else {
			// Unknown editor input
			throw new IllegalStateException("Unable to edit input " + input.getClass().getName());
		}

		// Specify initialise details
		this.envBridge = envBridge;

		// Create and initialise the editor
		this.editor = this.createEditor(envBridge);
		this.editor.init(new MvcFxUiModule(), (injector) -> {
			injector.injectMembers(this);
			return this.getDomain();
		});
		loadModel.accept(this.editor);

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
		this.editor.loadView((view) -> {
			this.getCanvas().setScene(new Scene(view));
		});
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		AbstractAdaptedEditorPart.this.editor.save();
	}

	@Override
	public void doSaveAs() {
		// TODO implement EditorPart.doSaveAs
		throw new UnsupportedOperationException("TODO implement EditorPart.doSaveAs");
	}

}