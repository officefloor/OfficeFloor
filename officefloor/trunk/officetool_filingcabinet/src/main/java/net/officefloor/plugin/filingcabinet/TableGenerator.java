/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.filingcabinet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import net.officefloor.model.repository.ConfigurationContext;

/**
 * Generates the class for the table.
 * 
 * @author Daniel Sagenschneider
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

		// Obtain the directory of the package
		String packagePath = this.table.getPackageName();
		if (packagePath != null) {
			packagePath = packagePath.replace('.', '/') + "/";
		}

		// Configure the table bean
		String tableBeanClassName = this.table.getSimpleClassName();
		ClassGenerator tableBean = new ClassGenerator(tableBeanClassName,
				this.table.getPackageName());
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

		// Write the table bean
		configurationContext.createConfigurationItem(packagePath
				+ tableBeanClassName + ".java", this.getInputStream(tableBean
				.generate()));

		// Configure the table repository
		String tableRepositoryClassName = this.table.getSimpleClassName()
				+ "Repository";
		ClassGenerator tableRepository = new ClassGenerator(
				tableRepositoryClassName, this.table.getPackageName());
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

		// Write the table repository
		configurationContext.createConfigurationItem(packagePath
				+ tableRepositoryClassName + ".java", this
				.getInputStream(tableRepository.generate()));

		// Configure and write the index beans
		for (AccessMetaData access : this.table.getAccesses()) {

			// Ignore access with only a single column.
			// (Input is type and no wrapper/container class necessary)
			if (access.getColumns().length == 1) {
				continue;
			}

			StringBuilder indexClassName = new StringBuilder();
			indexClassName.append(this.table.getSimpleClassName() + "Index");
			for (ColumnMetaData column : access.getColumns()) {
				indexClassName.append(FilingCabinetUtil
						.getSimpleClassName(column.getColumnName()));
			}
			ClassGenerator indexBean = new ClassGenerator(indexClassName
					.toString(), this.table.getPackageName());
			indexBean.addConstructor(access.getColumns());
			for (ColumnMetaData column : access.getColumns()) {
				indexBean.addProperty(column);
			}

			// Write the index bean
			configurationContext.createConfigurationItem(packagePath
					+ indexClassName.toString() + ".java", this
					.getInputStream(indexBean.generate()));
		}
	}

	/**
	 * Obtains an {@link InputStream} to the content.
	 * 
	 * @param content
	 *            Content for the {@link InputStream}.
	 * @return {@link InputStream}.
	 */
	private InputStream getInputStream(String content) {
		return new ByteArrayInputStream(content.getBytes());
	}

}
