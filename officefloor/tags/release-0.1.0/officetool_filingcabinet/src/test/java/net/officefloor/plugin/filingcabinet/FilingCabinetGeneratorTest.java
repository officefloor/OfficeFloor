/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Sagenschneider
 */
public class FilingCabinetGeneratorTest extends AbstractGeneratorTest {

	/**
	 * Ensure meta-data is correct.
	 */
	public void testMetaData() throws Exception {

		// Create the map of table meta-data
		Map<String, TableMetaData> tables = new HashMap<String, TableMetaData>();
		for (TableMetaData table : this.generator.getTableMetaData()) {
			tables.put(table.getFullyQualifiedClassName(), table);
		}

		// Validate Purchase table
		TableMetaData lineItemTable = tables.get(PACKAGE_PREFIX
				+ ".public_.purchaseorderlineitem.PurchaseOrderLineItem");

		// Expected columns of the purchase table
		final String[] COLUMN_PROPERTIES = new String[] { "getColumnName",
				"getSqlType", "getJavaType", "isNullable" };
		ColumnMetaData purchaseOrderId = new ColumnMetaData(
				"PURCHASE_ORDER_ID", 4, Integer.class, false);
		ColumnMetaData lineNumber = new ColumnMetaData("LINE_NUMBER", 4,
				Integer.class, false);
		ColumnMetaData productId = new ColumnMetaData("PRODUCT_ID", 4,
				Integer.class, false);
		ColumnMetaData quantity = new ColumnMetaData("QUANTITY", 4,
				Integer.class, false);
		ColumnMetaData priceAdjustment = new ColumnMetaData("PRICE_ADJUSTMENT",
				7, Float.class, true);
		ColumnMetaData comments = new ColumnMetaData("COMMENTS", 12,
				String.class, true);

		// Ensure columns are correct
		assertList(COLUMN_PROPERTIES, lineItemTable.getColumns(),
				purchaseOrderId, lineNumber, productId, quantity,
				priceAdjustment, comments);

		// Ensure primary key is correct
		assertList(COLUMN_PROPERTIES, lineItemTable.getPrimaryKey()
				.getColumns(), purchaseOrderId, lineNumber);

		// Ensure indexes are correct
		assertEquals("Incorrect number of indexes", 2, lineItemTable
				.getIndexes().length);
		assertList(COLUMN_PROPERTIES, lineItemTable.getIndexes()[0]
				.getColumns(), productId, quantity);

		// Ensure cross references are correct
		assertEquals("Incorrect number of cross references", 2, lineItemTable
				.getCrossReferences().length);
		CrossReferenceMetaData crossRef = lineItemTable.getCrossReferences()[0];
		assertEquals("Incorrect primary table", PACKAGE_PREFIX
				+ ".public_.productprice.ProductPrice", crossRef
				.getPrimaryTable().getFullyQualifiedClassName());
		assertList(COLUMN_PROPERTIES, crossRef.getPrimaryColumns(),
				new ColumnMetaData("PRODUCT_ID", 4, Integer.class, false),
				new ColumnMetaData("QUANTITY", 4, Integer.class, false));
		assertEquals("Incorrect cross reference table", PACKAGE_PREFIX
				+ ".public_.purchaseorderlineitem.PurchaseOrderLineItem",
				crossRef.getForeignTable().getFullyQualifiedClassName());
		assertList(COLUMN_PROPERTIES, crossRef.getForeignColumns(), productId,
				quantity);
	}

	/**
	 * Aiding in understanding {@link DatabaseMetaData}.
	 */
	public void testShowDetails() throws Exception {

		// Output the listing of tables
		for (TableMetaData table : this.generator.getTableMetaData()) {

			// Ignore information schema
			if ("INFORMATION_SCHEMA".equals(table.getSchemaName())) {
				continue;
			}

			// Output table information
			System.out.println();
			System.out.print("TABLE: " + table.getTableName() + " [ ");
			for (ColumnMetaData primaryKey : table.getPrimaryKey().getColumns()) {
				System.out.print(primaryKey.getColumnName() + " ");
			}
			System.out.println("]");
			for (ColumnMetaData column : table.getColumns()) {
				System.out.println("  " + column.getColumnName() + " ["
						+ column.getSqlType() + "="
						+ column.getJavaType().getSimpleName() + "]"
						+ (column.isNullable() ? " NULLABLE" : ""));
			}
			for (IndexMetaData index : table.getIndexes()) {
				System.out.print((index.isUnique() ? " index: " : " unique: ")
						+ index.getIndexName() + " [ ");
				for (ColumnMetaData column : index.getColumns()) {
					System.out.print(column.getColumnName() + " ");
				}
				System.out.println("]");
			}
			for (CrossReferenceMetaData crossReference : table
					.getCrossReferences()) {
				System.out
						.print(" xref: "
								+ crossReference.getForeignKeyName()
								+ "  "
								+ crossReference.getPrimaryTable()
										.getTableName() + "[");
				boolean isFirst = true;
				for (ColumnMetaData column : crossReference.getPrimaryColumns()) {
					if (isFirst) {
						isFirst = false;
					} else {
						System.out.print(",");
					}
					System.out.print(column.getColumnName());
				}
				System.out
						.print("] "
								+ crossReference.getForeignTable()
										.getTableName() + "[");
				isFirst = true;
				for (ColumnMetaData column : crossReference.getForeignColumns()) {
					if (isFirst) {
						isFirst = false;
					} else {
						System.out.print(",");
					}
					System.out.print(column.getColumnName());
				}
				System.out.println("]");
			}

			// Output data class information
			System.out.println("DATA CLASS: "
					+ table.getFullyQualifiedClassName());
			for (ColumnMetaData column : table.getColumns()) {
				System.out.println("  " + column.getJavaType().getSimpleName()
						+ " " + column.getGetMethodName() + "()");
				System.out.println("  void " + column.getSetMethodName() + "("
						+ column.getJavaType().getSimpleName() + " "
						+ column.getFieldName() + ")");
			}
			for (CrossReferenceMetaData crossReference : table
					.getCrossReferences()) {
				TableMetaData foreignTable = (crossReference.getPrimaryTable() == table ? crossReference
						.getForeignTable()
						: crossReference.getPrimaryTable());
				System.out.println("  void linkTo"
						+ foreignTable.getSimpleClassName() + "("
						+ foreignTable.getSimpleClassName() + " link)");
			}

			// Output repository class information
			System.out.println("REPOSITORY CLASS: "
					+ table.getFullyQualifiedClassName() + "Repository");
			System.out.println("  "
					+ this.getRetrieveSignature(table, table.getPrimaryKey()));
			for (IndexMetaData index : table.getIndexes()) {
				System.out.println("  "
						+ this.getRetrieveSignature(table, index));
			}
			for (CrossReferenceMetaData crossReference : table
					.getCrossReferences()) {
				System.out.println("  "
						+ this.getRetrieveSignature(table, crossReference));
			}
			System.out.println("  void create" + table.getSimpleClassName()
					+ "(" + table.getSimpleClassName()
					+ " item) // may set id if primary key generated by db");
			System.out.println("  void update" + table.getSimpleClassName()
					+ "(" + table.getSimpleClassName() + " item)");
			System.out.println("  void delete" + table.getSimpleClassName()
					+ "(" + table.getSimpleClassName() + " item)");
		}
	}

	/**
	 * Obtains the retrieve method signature.
	 * 
	 * @param table
	 *            {@link TableMetaData}.
	 * @param index
	 *            {@link IndexMetaData}.
	 * @return Retrieve method signature.
	 */
	private String getRetrieveSignature(TableMetaData table, IndexMetaData index) {
		StringBuilder signature = new StringBuilder();
		if (index.isUnique()) {
			signature.append(table.getSimpleClassName());
		} else {
			signature.append("List<" + table.getSimpleClassName() + ">");
		}
		signature.append(" retrieveBy");
		for (ColumnMetaData column : index.getColumns()) {
			signature.append(FilingCabinetUtil.getSimpleClassName(column
					.getColumnName()));
		}
		signature.append("(");
		if (index.getColumns().length == 1) {
			signature.append(index.getColumns()[0].getJavaType()
					.getSimpleName());
		} else {
			signature.append(index.getSimpleClassName());
		}
		signature.append(" key, Connection connection)");
		return signature.toString();
	}

	/**
	 * Obtains the retrieve method signature.
	 * 
	 * @param table
	 *            {@link TableMetaData}.
	 * @param crossReference
	 *            {@link CrossReferenceMetaData}.
	 * @return Retrieve method signature.
	 */
	private String getRetrieveSignature(TableMetaData table,
			CrossReferenceMetaData crossReference) {
		StringBuilder signature = new StringBuilder();
		signature.append("List<" + table.getSimpleClassName() + "> retrieveBy");
		TableMetaData foreignTable = (table == crossReference.getPrimaryTable() ? crossReference
				.getForeignTable()
				: crossReference.getPrimaryTable());
		signature.append(foreignTable.getSimpleClassName() + "("
				+ foreignTable.getSimpleClassName()
				+ " key, Connection connection)");
		return signature.toString();
	}

}
