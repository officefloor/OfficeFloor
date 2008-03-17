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
package net.officefloor.plugin.filingcabinet;

import net.officefloor.repository.ConfigurationContext;

/**
 * Generates the class for the table.
 * 
 * @author Daniel
 */
public class TableGenerator {

	/**
	 * {@link TableMetaData}.
	 */
	private final TableMetaData table;

	/**
	 * Initialise.
	 * 
	 * @param table
	 *            {@link TableMetaData}.
	 */
	public TableGenerator(TableMetaData table) {
		this.table = table;
	}

	/**
	 * Generates the classes for the {@link TableMetaData} initialised with.
	 * 
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @throws Exception
	 *             If fails to generate the classes.
	 */
	public void generate(ConfigurationContext configurationContext)
			throws Exception {

		// Write the table bean
		ClassGenerator tableBean = new ClassGenerator(this.table
				.getSimpleClassName(), this.table.getPackageName());
		for (ColumnMetaData column : this.table.getColumns()) {
			tableBean.addProperty(column);
		}
		for (CrossReferenceMetaData xref : this.table.getCrossReferences()) {
			if (xref.getPrimaryTable() == this.table) {
				tableBean.addLinkTo(xref.getPrimaryColumns(), xref
						.getForeignTable(), xref.getForeignColumns());
			} else {
				tableBean.addLinkTo(xref.getForeignColumns(), xref
						.getPrimaryTable(), xref.getPrimaryColumns());
			}
		}
		tableBean.addLoad(this.table);

		// Write the message bean
		ClassGenerator tableRepository = new ClassGenerator(this.table
				.getSimpleClassName()
				+ "Repository", this.table.getPackageName());
		tableRepository.addRetrieve(this.table);
		tableRepository.addRetrieveList(this.table);
		for (AccessMetaData access : this.table.getAccesses()) {
			tableRepository.addRetrieveBy(this.table, access);
		}
		for (CrossReferenceMetaData xref : this.table.getCrossReferences()) {
			boolean isPrimary = (this.table == xref.getPrimaryTable());
			TableMetaData table = (isPrimary ? xref.getPrimaryTable() : xref
					.getForeignTable());
			ColumnMetaData[] columns = (isPrimary ? xref.getPrimaryColumns()
					: xref.getForeignColumns());
			TableMetaData linkTable = (isPrimary ? xref.getForeignTable()
					: xref.getPrimaryTable());
			ColumnMetaData[] linkColumns = (isPrimary ? xref
					.getForeignColumns() : xref.getPrimaryColumns());

			// Add only if able to access
			if (this.table.getAccess(columns) != null) {
				tableRepository.addRetrieveFrom(table, columns, linkTable,
						linkColumns);
			}
		}

		// Return the contents
		System.out.println("TABLE BEAN:\n" + tableBean.generate());
		System.out.println("TABLE REPOSITORY:\n" + tableRepository.generate());
	}

}
