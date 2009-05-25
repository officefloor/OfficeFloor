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
package net.officefloor.eclipse.common.drag;

import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * Provides a {@link org.eclipse.ui.views.navigator.LocalSelectionTransfer} drag
 * source implementation.
 * 
 * @author Daniel
 */
public class LocalSelectionTransferDragSourceListener extends DragSourceAdapter
		implements TransferDragSourceListener {

	/**
	 * {@link ISelectionProvider} to obtain the {@link ISelection} to transfer.
	 */
	protected final ISelectionProvider selectionProvider;

	/**
	 * Initiate with {@link ISelectionProvider} to obtain the {@link ISelection}
	 * to transfer.
	 * 
	 * @param selectionProvider
	 *            {@link ISelectionProvider}.
	 */
	public LocalSelectionTransferDragSourceListener(
			ISelectionProvider selectionProvider) {
		// Store state
		this.selectionProvider = selectionProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.util.TransferDragSourceListener#getTransfer()
	 */
	public Transfer getTransfer() {
		return LocalSelectionTransfer.getInstance();
	}

	/*
	 * non Java-doc
	 * 
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragStart
	 */
	public void dragStart(DragSourceEvent event) {

		// Obtain the selection
		ISelection selection = this.selectionProvider.getSelection();

		// Initiate the local transfer
		LocalSelectionTransfer.getInstance().setSelection(selection);
		LocalSelectionTransfer.getInstance().setSelectionSetTime(
				event.time & 0xFFFFFFFFL);
		event.doit = isDragable(selection);
	}

	/**
	 * Checks if the elements within may be dragged.
	 * 
	 * @param selection
	 *            Elements to be dragged.
	 */
	protected boolean isDragable(ISelection selection) {
		return true;
	}

	/*
	 * non Java-doc
	 * 
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData
	 */
	public void dragSetData(DragSourceEvent event) {
		// Specify the selection on the event for consistency
		event.data = LocalSelectionTransfer.getInstance().getSelection();
	}

	/*
	 * non Java-doc
	 * 
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished
	 */
	public void dragFinished(DragSourceEvent event) {
		// Make sure we do not have to do any remaining work
		// Assert.isTrue(event.detail != DND.DROP_MOVE);
		LocalSelectionTransfer.getInstance().setSelection(null);
		LocalSelectionTransfer.getInstance().setSelectionSetTime(0);
	}

}
