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
package net.officefloor.eclipse.ide.editor;

import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.util.function.Function;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.gef.fx.swt.canvas.FXCanvasEx;
import org.eclipse.gef.fx.swt.canvas.IFXCanvasFactory;
import org.eclipse.gef.mvc.fx.ui.MvcFxUiModule;
import org.eclipse.gef.mvc.fx.ui.parts.AbstractFXEditor;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.dialogs.SaveAsDialog;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.memory.MemoryConfigurationContext;
import net.officefloor.eclipse.configurer.AbstractConfigurerRunnable;
import net.officefloor.eclipse.editor.AdaptedBuilder;
import net.officefloor.eclipse.editor.AdaptedEditorModule;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.AdaptedRootBuilder;
import net.officefloor.eclipse.editor.ChangeAdapter;
import net.officefloor.eclipse.editor.ChangeExecutor;
import net.officefloor.eclipse.ide.editor.AbstractParentConfigurableItem.ConfigurableContext;
import net.officefloor.eclipse.osgi.OfficeFloorOsgiBridge;
import net.officefloor.eclipse.osgi.ProjectConfigurationContext;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.OfficeFloorModel;

/**
 * {@link OfficeFloorModel} editor.
 * 
 * @param <R>
 *            Root {@link Model} type.
 * @param <O>
 *            {@link Change} operations type.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractIdeEditor<R extends Model, RE extends Enum<RE>, O> extends AbstractFXEditor {

	/**
	 * Indicates if running outside the {@link IWorkbench}.
	 */
	private static boolean isOutsideWorkbench = false;

	/**
	 * Logic to launch outside the {@link IWorkbench}.
	 */
	public static interface OutsideWorkbenchLauncher {

		/**
		 * Launches outside the {@link IWorkbench}.
		 * 
		 * @throws Throwable
		 *             If failure in launching.
		 */
		void launch() throws Throwable;
	}

	/**
	 * Enables running the {@link AbstractIdeEditor} outside of the
	 * {@link IWorkbench}.
	 * 
	 * @param launcher
	 *            {@link OutsideWorkbenchLauncher}.
	 */
	public synchronized static void launchOutsideWorkbench(OutsideWorkbenchLauncher launcher) {
		isOutsideWorkbench = true;
		try {
			launcher.launch();
		} catch (Throwable ex) {
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			} else {
				throw new RuntimeException(ex);
			}
		} finally {
			isOutsideWorkbench = false;
		}
	}

	/**
	 * {@link Module} for running outside the {@link IWorkbench}.
	 */
	private static class OutsideWorkbenchModule extends AbstractModule implements IFXCanvasFactory {

		@Override
		protected void configure() {
			this.bind(IFXCanvasFactory.class).toInstance(this);
		}

		@Override
		public FXCanvas createCanvas(Composite parent, int style) {
			return new FXCanvasEx(parent, style);
		}
	}

	/**
	 * Convenience method to launch the {@link AbstractIdeEditor} implementation
	 * outside the {@link IWorkbench}.
	 * 
	 * @param configuration
	 *            Configuration of {@link ConfigurationItem} to launch the
	 *            {@link AbstractIdeEditor}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void launch(String configuration) {

		// Determine the launch class
		StackTraceElement[] cause = Thread.currentThread().getStackTrace();
		boolean isFoundThisMethod = false;
		String launchClassName = null;
		FOUND_LAUNCHER: for (StackTraceElement se : cause) {
			String className = se.getClassName();
			String methodName = se.getMethodName();
			if (isFoundThisMethod) {
				launchClassName = className;
				break FOUND_LAUNCHER;
			} else if (AbstractIdeEditor.class.getName().equals(className) && "launch".equals(methodName)) {
				isFoundThisMethod = true;
			}
		}

		// Ensure have the launch class
		if (launchClassName == null) {
			throw new RuntimeException("Unable to determine " + AbstractIdeEditor.class.getName() + " implementation");
		}

		// Attempt to lauch IDE editor outside workbench
		final String finalLaunchClassName = launchClassName;
		launchOutsideWorkbench(() -> {

			// Instantiate the editor
			Class launchClass = Class.forName(finalLaunchClassName, false,
					Thread.currentThread().getContextClassLoader());
			Object instance = launchClass.newInstance();
			AbstractIdeEditor editor = (AbstractIdeEditor) instance;

			// Load configuration to run outside workbench
			editor.osgiBridge = OfficeFloorOsgiBridge.getClassLoaderInstance();
			editor.configurationItem = MemoryConfigurationContext.createWritableConfigurationItem("TEST");
			editor.configurationItem.setConfiguration(new ByteArrayInputStream(configuration.getBytes()));

			// Display the editor
			AbstractConfigurerRunnable runnable = new AbstractConfigurerRunnable() {

				@Override
				protected void loadConfiguration(Shell shell) {
					editor.parentShell = shell;
					editor.createPartControl(shell);

					// Register logging of changes to the model
					editor.rootBuilder.getChangeExecutor().addChangeListener(new ChangeAdapter() {

						@Override
						public void postApply(Change<?> change) {

							// Save model (with changes applied)
							editor.doSave(null);

							// Log changed configuration for tracking
							try {
								System.out.println();
								System.out.println();
								System.out.println(
										"============== Change " + change.getChangeDescription() + "==============");
								Reader reader = editor.configurationItem.getReader();
								for (int character = reader.read(); character != -1; character = reader.read()) {
									System.out.write(character);
								}
								System.out.println();
								System.out.println();
							} catch (Exception ex) {
								System.err.println("Failed to log configuration: ");
								ex.printStackTrace();
							}
						}
					});
				}
			};
			runnable.run();
		});
	}

	/**
	 * {@link Injector}.
	 */
	private final Injector injector;

	/**
	 * {@link AdaptedEditorModule}.
	 */
	private AdaptedEditorModule module;

	/**
	 * {@link AdaptedBuilder}.
	 */
	private AdaptedBuilder adaptedBuilder;

	/**
	 * {@link Function} to create the operations from the root {@link Model}.
	 */
	private Function<R, O> createOperations;

	/**
	 * Current {@link IFile} being edited.
	 */
	private IFile configurationFile;

	/**
	 * {@link WritableConfigurationItem}.
	 */
	private WritableConfigurationItem configurationItem;

	/**
	 * Parent {@link Shell}.
	 */
	private Shell parentShell;

	/**
	 * {@link AdaptedRootBuilder}.
	 */
	private AdaptedRootBuilder<R, O> rootBuilder;

	/**
	 * {@link Model} being edited.
	 */
	private R model;

	/**
	 * Operations.
	 */
	private O operations;

	/**
	 * {@link OfficeFloorOsgiBridge}.
	 */
	private OfficeFloorOsgiBridge osgiBridge;

	/**
	 * Instantiate to capture {@link Injector}.
	 * 
	 * @param injector
	 *            {@link Injector}.
	 */
	private AbstractIdeEditor(Injector injector) {
		super(injector);
		this.injector = injector;
	}

	/**
	 * <p>
	 * Instantiate to capture {@link AdaptedEditorModule}.
	 * <p>
	 * Allows for alternate {@link AdaptedEditorModule} implementation.
	 * 
	 * @param module
	 *            {@link AdaptedEditorModule}.
	 * @param rootModelType
	 *            Root {@link Model} type.
	 * @param createOperations
	 *            {@link Function} to create the operations from the root
	 *            {@link Model}.
	 */
	protected AbstractIdeEditor(AdaptedEditorModule module, Class<R> rootModelType, Function<R, O> createOperations) {
		this(Guice.createInjector(isOutsideWorkbench ? Modules.override(module).with(new OutsideWorkbenchModule())
				: Modules.override(module).with(new MvcFxUiModule())));
		this.module = module;
		this.createOperations = createOperations;

		// Initialise
		this.module.initialise(this.getDomain(), this.injector);

		// Create the adapted builder
		this.adaptedBuilder = (context) -> {

			// Create the root model
			this.rootBuilder = context.root(rootModelType, (model) -> {
				// Keep track of operations (for configurable context)
				this.operations = this.createOperations(model);
				return this.operations;
			});

			// Configure rest of editor
			for (AbstractParentConfigurableItem<R, RE, O, ?, ?, ?> parent : this.getParents()) {

				// Initialise the parent
				parent.init(new ConfigurableContext<R, O>() {

					@Override
					public AdaptedRootBuilder<R, O> getRootBuilder() {
						return AbstractIdeEditor.this.rootBuilder;
					}

					@Override
					public OfficeFloorOsgiBridge getOsgiBridge() throws Exception {
						return AbstractIdeEditor.this.getOsgiBridge();
					}

					@Override
					public Shell getParentShell() {
						return AbstractIdeEditor.this.parentShell;
					}

					@Override
					public O getOperations() {
						return AbstractIdeEditor.this.operations;
					}

					@Override
					public ChangeExecutor getChangeExecutor() {
						return AbstractIdeEditor.this.rootBuilder.getChangeExecutor();
					}
				});

				// Create the adapted parent
				AdaptedParentBuilder<R, O, ?, ?> parentBuilder = parent.createAdaptedParent();

				// Consider creating children of parent

			}
		};
	}

	/**
	 * Instantiate with default {@link AdaptedEditorModule}.
	 * 
	 * @param rootModelType
	 *            Root {@link Model} type.
	 * @param createOperations
	 *            {@link Function} to create the operations from the root
	 *            {@link Model}.
	 */
	public AbstractIdeEditor(Class<R> rootModelType, Function<R, O> createOperations) {
		this(new AdaptedEditorModule(), rootModelType, createOperations);
	}

	/**
	 * Obtains the {@link AbstractParentConfigurableItem} instances.
	 * 
	 * @return {@link AbstractParentConfigurableItem} instances.
	 */
	protected abstract AbstractParentConfigurableItem<R, RE, O, ?, ?, ?>[] getParents();

	/**
	 * Creates the root {@link Model} from the {@link ConfigurationItem}.
	 * 
	 * @param configurationItem
	 *            {@link ConfigurationItem} containing the configuration of the
	 *            {@link Model}.
	 * @return Root {@link Model} within the {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to load the root {@link Model} from the
	 *             {@link ConfigurationItem}.
	 */
	protected abstract R loadRootModel(ConfigurationItem configurationItem) throws Exception;

	/**
	 * Writes the root {@link Model} to the {@link WritableConfigurationItem}.
	 * 
	 * @param model
	 *            Root {@link Model} to be saved.
	 * @param configurationItem
	 *            {@link WritableConfigurationItem}.
	 * @throws Exception
	 *             If fails to save the root {@link Model} into the
	 *             {@link WritableConfigurationItem}.
	 */
	protected abstract void saveRootModel(R model, WritableConfigurationItem configurationItem) throws Exception;

	/**
	 * Creates the operations for the root {@link Model}.
	 * 
	 * @param model
	 *            Root {@link Model}.
	 * @return Operations.
	 */
	public O createOperations(R model) {
		return this.createOperations.apply(model);
	}

	/**
	 * Obtains the {@link OfficeFloorOsgiBridge}.
	 * 
	 * @return {@link OfficeFloorOsgiBridge}.
	 * @throws Exception
	 *             If fails to obtain the {@link OfficeFloorOsgiBridge}.
	 */
	protected OfficeFloorOsgiBridge getOsgiBridge() throws Exception {

		// Determine if cached access to compiler
		if (this.osgiBridge != null) {
			return this.osgiBridge;
		}

		// Obtain the file input
		IEditorInput input = this.getEditorInput();
		if (!(input instanceof IFileEditorInput)) {
			throw new Exception(
					"Invalid IEditorInput as expecting a file (" + (input == null ? null : input.getClass().getName()));
		}
		IFileEditorInput fileInput = (IFileEditorInput) input;

		// Obtain the file (and subsequently it's project)
		IFile file = fileInput.getFile();
		IProject project = file.getProject();

		// Obtain the java project
		IJavaProject javaProject = JavaCore.create(project);

		// Bridge java project to OfficeFloor compiler
		this.osgiBridge = new OfficeFloorOsgiBridge(javaProject);

		// Obtain the OfficeFloor compiler
		return this.osgiBridge;
	}

	/*
	 * ============== AbstractFXEditor ==================
	 */

	@Override
	public IViewer getContentViewer() {
		return this.module.getContentViewer();
	}

	@Override
	protected void hookViewers() {

		// Create the view
		Pane view = this.module.createParent(this.adaptedBuilder);

		// Create scene and populate canvas with view
		this.getCanvas().setScene(new Scene(view));
	}

	@Override
	protected void activate() {

		// Load the model
		this.rootBuilder.getErrorHandler().isError(() -> {
			this.model = this.loadRootModel(this.configurationItem);
		});

		// Load the module with root model
		this.module.loadRootModel(this.model);

		// Activate
		super.activate();
	}

	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);

		// Input changed, so reset for new input
		this.osgiBridge = null;

		// Obtain the input configuration
		IFileEditorInput fileInput = (IFileEditorInput) input;
		this.configurationFile = fileInput.getFile();
		this.configurationItem = ProjectConfigurationContext.getWritableConfigurationItem(this.configurationFile, null);
	}

	@Override
	protected void setSite(IWorkbenchPartSite site) {
		super.setSite(site);

		// Keep track of the parent shell
		this.parentShell = site.getShell();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		this.rootBuilder.getErrorHandler().isError(() -> {

			// Save the model
			this.saveRootModel(this.model, this.configurationItem);

			// Flag saved (no longer dirty)
			this.markNonDirty();
		});
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void doSaveAs() {
		// Allow saving a copy of the configuration
		final SaveAsDialog saveAsDialog = new SaveAsDialog(this.getEditorSite().getShell());
		saveAsDialog.setOriginalFile(this.configurationFile);
		if (saveAsDialog.open() == IStatus.OK) {

			// Save to the specified file
			IPath file = saveAsDialog.getResult();

			// TODO handle saving to the path
			System.out.println("TODO implement SaveAs to " + file.toString());
		}
	}

}