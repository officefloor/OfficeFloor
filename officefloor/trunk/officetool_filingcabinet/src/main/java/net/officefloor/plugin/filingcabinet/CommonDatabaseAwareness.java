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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * Common awareness of a database.
 * 
 * @author Daniel
 */
public class CommonDatabaseAwareness implements DatabaseAwareness {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.filingcabinet.DatabaseAwareness#getJavaType(int)
	 */
	@Override
	public Class<?> getJavaType(int sqlType) throws Exception {
		switch (sqlType) {
		case Types.CHAR:
			return String.class;
		case Types.VARCHAR:
			return String.class;
		case Types.LONGVARCHAR:
			return String.class;
		case Types.NUMERIC:
			return BigDecimal.class;
		case Types.DECIMAL:
			return BigDecimal.class;
		case Types.BIT:
			return Boolean.class;
		case Types.TINYINT:
			return Byte.class;
		case Types.SMALLINT:
			return Short.class;
		case Types.INTEGER:
			return Integer.class;
		case Types.BIGINT:
			return Long.class;
		case Types.REAL:
			return Float.class;
		case Types.FLOAT:
			return Double.class;
		case Types.DOUBLE:
			return Double.class;
		case Types.BINARY:
			return new byte[0].getClass();
		case Types.VARBINARY:
			return new byte[0].getClass();
		case Types.LONGVARBINARY:
			return new byte[0].getClass();
		case Types.DATE:
			return Date.class;
		case Types.TIME:
			return Time.class;
		case Types.TIMESTAMP:
			return Timestamp.class;
		case Types.BOOLEAN:
			return Boolean.class;
		default:
			throw new Exception("Unable to obtain java class for sql type "
					+ sqlType);
		}
	}

}
