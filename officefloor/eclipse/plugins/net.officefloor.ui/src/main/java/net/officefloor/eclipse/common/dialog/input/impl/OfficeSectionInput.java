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
package net.officefloor.eclipse.common.dialog.input.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.OfficeTaskType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputContext;

/**
 * {@link Input} to obtain an item within the {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSectionInput implements Input<Tree> {

	/**
	 * {@link OfficeSectionType}.
	 */
	private final OfficeSectionType officeSectionType;

	/**
	 * Listing of the types of objects within the {@link OfficeSectionType} that
	 * may be selected for returning.
	 */
	private final Class<?>[] selectionTypes;

	/**
	 * Initiate.
	 * 
	 * @param officeSectionType
	 *            {@link OfficeSectionType}.
	 * @param selectionTypes
	 *            Listing of the types of objects within the
	 *            {@link OfficeSection} that may be selected for returning.
	 */
	public OfficeSectionInput(OfficeSectionType officeSectionType, Class<?>... selectionTypes) {
		this.officeSectionType = officeSectionType;
		this.selectionTypes = selectionTypes;
	}

	/*
	 * ====================== Input ================================
	 */

	@Override
	public Tree buildControl(final InputContext context) {
		// Create the tree
		final Tree tree = new Tree(context.getParent(), SWT.NONE);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Create the Tree selection
		TreeViewer treeViewer = new TreeViewer(tree);
		treeViewer.setAutoExpandLevel(2);
		treeViewer.setContentProvider(new OfficeSectionTreeContentProvider());
		treeViewer.setLabelProvider(new OfficeSectionLabelProvider());
		treeViewer.setInput(this.officeSectionType);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// Notify of change
				context.notifyValueChanged(OfficeSectionInput.this.getValue(tree, context));
			}
		});

		// Return the tree
		return tree;
	}

	@Override
	public Object getValue(Tree control, InputContext context) {

		// Obtain the selection from the tree
		TreeItem[] treeItems = control.getSelection();

		// Ensure only one tree item selected
		if (treeItems.length != 1) {
			return null;
		}

		// Obtain the data of selection
		TreeItem treeItem = treeItems[0];
		Object data = treeItem.getData();

		// Determine if restricting selection
		if (this.selectionTypes.length == 0) {
			// No restriction, so may select any
			return data;
		}

		// Determine if of type being looked for
		for (Class<?> selectionType : this.selectionTypes) {
			if (selectionType.isInstance(data)) {
				// Right type so may select
				return data;
			}
		}

		// If at this point, restricted from being selected
		return null;
	}

	/**
	 * {@link ITreeContentProvider} for the {@link OfficeSection}.
	 */
	private class OfficeSectionTreeContentProvider implements ITreeContentProvider {

		/**
		 * Keeps track of the child to parent.
		 */
		private Map<Object, Object> childToParent = new HashMap<Object, Object>();

		/*
		 * ================ ITreeContentProvider ======================
		 */

		@Override
		public Object[] getElements(Object inputElement) {
			return this.getChildren(inputElement);
		}

		@Override
		public boolean hasChildren(Object element) {
			return (this.getChildren(element).length > 0);
		}

		@Override
		public Object[] getChildren(Object parent) {

			// Create the listing of children
			List<Object> children = new LinkedList<Object>();
			if (parent instanceof OfficeSubSectionType) {
				OfficeSubSectionType subSectionType = (OfficeSubSectionType) parent;
				children.addAll(Arrays.asList(subSectionType.getOfficeTaskTypes()));
				children.addAll(Arrays.asList(subSectionType.getOfficeSubSectionTypes()));

			} else {
				// Unknown type, so no children
			}

			// Map all children to parent
			for (Object child : children) {
				this.childToParent.put(child, parent);
			}

			// Return the children
			return children.toArray(new Object[0]);
		}

		@Override
		public Object getParent(Object element) {
			return this.childToParent.get(element);
		}

		@Override
		public void dispose() {
			// Nothing to dispose
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Input should never change
		}
	}

	/**
	 * {@link OfficeSection} {@link ILabelProvider}.
	 */
	private class OfficeSectionLabelProvider extends BaseLabelProvider implements ILabelProvider {

		/*
		 * ================= ILabelProvider ============================
		 */

		@Override
		public Image getImage(Object element) {
			// TODO provide images to aid easier selection
			return null;
		}

		@Override
		public String getText(Object element) {
			// Return description
			if (element instanceof OfficeSubSectionType) {
				return "Section: " + ((OfficeSubSection) element).getOfficeSectionName();
			} else if (element instanceof OfficeTaskType) {
				return "Task: " + ((OfficeTaskType) element).getOfficeTaskName();
			} else {
				// Unknown type
				return "UNKNOWN TYPE: " + element + " [" + (element == null ? null : element.getClass().getName())
						+ "]";
			}
		}
	}

}