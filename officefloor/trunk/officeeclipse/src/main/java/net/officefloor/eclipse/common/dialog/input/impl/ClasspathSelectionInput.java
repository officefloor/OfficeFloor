/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.common.dialog.input.impl;

import net.officefloor.eclipse.classpath.ClasspathUtil;
import net.officefloor.eclipse.common.dialog.input.ClasspathFilter;
import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputContext;
import net.officefloor.eclipse.common.dialog.input.filter.ClassNameInputFilter;
import net.officefloor.eclipse.common.dialog.input.filter.FileNameInputFilter;
import net.officefloor.eclipse.repository.project.ProjectConfigurationContext;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * {@link Input} to obtain an item from the class path.
 * 
 * @author Daniel
 */
public class ClasspathSelectionInput implements Input<Composite> {

	/**
	 * {@link IProject} as root of {@link Tree}. If <code>null</code> includes
	 * all open {@link IProject} instances of the {@link IWorkspaceRoot}.
	 */
	private final IProject project;

	/**
	 * {@link ClasspathFilter}.
	 */
	private final ClasspathFilter filter;

	/**
	 * Flag indicating to output the name of the object selected rather than the
	 * object itself.
	 */
	private final boolean isOutputName;

	/**
	 * {@link Tree}.
	 */
	private Tree tree;

	/**
	 * Initiate to use {@link IWorkspaceRoot}.
	 */
	public ClasspathSelectionInput() {
		this((IProject) null);
	}

	/**
	 * Initiate with {@link IProject} as root.
	 * 
	 * @param project
	 *            {@link IProject} to be root.
	 */
	public ClasspathSelectionInput(IProject project) {
		this(project, true);
	}

	/**
	 * Initiate with {@link IProject} as root.
	 * 
	 * @param project
	 *            {@link IProject} to be root.
	 * @param isOutputName
	 *            Indicates whether to return the name or the {@link IResource}/
	 *            {@link IJavaElement}.
	 */
	public ClasspathSelectionInput(IProject project, boolean isOutputName) {
		this.project = project;
		this.filter = new ClasspathFilter();
		this.isOutputName = isOutputName;
	}

	/**
	 * Initiate with the {@link IProject} of the {@link IEditorInput}.
	 * 
	 * @param editor
	 *            {@link IEditorInput}.
	 */
	public ClasspathSelectionInput(IEditorPart editor) {
		this(ProjectConfigurationContext.getProject(editor.getEditorInput()));
	}

	/**
	 * Obtains the {@link ClasspathFilter}.
	 * 
	 * @return {@link ClasspathFilter}.
	 */
	public ClasspathFilter getClasspathFilter() {
		return this.filter;
	}

	/**
	 * Obtains the item that is the root of the tree.
	 * 
	 * @return Item to be the root of the tree.
	 */
	public Object getTreeRootItem() {
		// Obtain the root of the tree
		if (this.project == null) {
			// Use the workspace root
			return ResourcesPlugin.getWorkspace().getRoot();

		} else {
			// Use the specified project
			Object specificProject = ClasspathUtil
					.getSpecificProject(this.project);

			// Return the specific project
			return specificProject;
		}
	}

	/*
	 * ================= Input ===================================
	 */

	@Override
	public Composite buildControl(final InputContext context) {

		// Create composite to contain file expression and selection tree
		final Composite container = new Composite(context.getParent(), SWT.NONE);
		container.setLayout(new GridLayout(1, false));

		// Add filters for file and class names
		final FileNameInputFilter fileNameFilter = new FileNameInputFilter();
		this.filter.addFileFilter(fileNameFilter);
		final ClassNameInputFilter classNameFilter = new ClassNameInputFilter();
		this.filter.addClassFileFilter(classNameFilter);

		// Create the text box to provide filtering regular expression
		Composite filterContainer = new Composite(container, SWT.NONE);
		filterContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
				true, false));
		filterContainer.setLayout(new GridLayout(2, false));
		new Label(filterContainer, SWT.NONE).setText("Filter");
		final Text text = new Text(filterContainer, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		// Create the tree
		this.tree = new Tree(container, SWT.NONE);
		this.tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Obtain the root of the tree
		final Object treeInput = this.getTreeRootItem();

		// Create the Tree selection
		final TreeViewer treeViewer = new TreeViewer(this.tree);
		treeViewer.setAutoExpandLevel(1);
		treeViewer.setContentProvider(new ClasspathWorkbenchContentProvider());
		treeViewer.setLabelProvider(WorkbenchLabelProvider
				.getDecoratingWorkbenchLabelProvider());
		treeViewer.setInput(treeInput);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// Notify of change
				context.notifyValueChanged(ClasspathSelectionInput.this
						.getValue(container, context));
			}
		});

		// Add text listener that filters on changed details
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Change filter as per text
				String includeText = text.getText();
				fileNameFilter.setFileNameRegularExpression(includeText);
				classNameFilter.setClassNameRegularExpression(includeText);

				// Reset tree input to take into account filtering
				treeViewer.refresh(false);
			}
		});

		// Return the tree
		return this.tree;
	}

	@Override
	public Object getValue(Composite control, InputContext context) {

		// Obtain the selection from the tree
		TreeItem[] treeItems = this.tree.getSelection();

		// Ensure only one tree item selected
		if (treeItems.length != 1) {
			return null;
		}

		// Obtain the data of selection
		TreeItem treeItem = treeItems[0];
		Object data = treeItem.getData();

		// Return based on specific type
		if (data instanceof IResource) {
			IResource resource = (IResource) data;
			if (this.isOutputName) {
				return ClasspathUtil.getClassPathLocation(resource
						.getFullPath());
			} else {
				return resource;
			}

		} else if (data instanceof IJavaElement) {
			IJavaElement javaElement = (IJavaElement) data;
			if (this.isOutputName) {
				return ClasspathUtil.getClassName(javaElement);
			} else {
				return javaElement;
			}

		} else {
			// Unknown type
			return null;
		}
	}

	/**
	 * Class path {@link WorkbenchContentProvider}.
	 */
	private class ClasspathWorkbenchContentProvider extends
			WorkbenchContentProvider {

		/*
		 * ================ BaseWorkbenchContentProvider ===================
		 */

		@Override
		public Object[] getChildren(Object o) {
			try {

				// Obtain the children
				Object[] children = ClasspathUtil.getChildren(o);

				// Filter based on descendants
				Object[] includedChildren = ClasspathSelectionInput.this.filter
						.descendantFilter(children);

				// Return the filtered children
				return includedChildren;

			} catch (Exception ex) {

				// Indicate error
				MessageDialog.openError(null, "Error",
						"Failed to get children: " + ex.getMessage() + " ["
								+ this.getClass().getSimpleName() + "]");

				// Failed to get children
				return new Object[0];
			}
		}
	}

}