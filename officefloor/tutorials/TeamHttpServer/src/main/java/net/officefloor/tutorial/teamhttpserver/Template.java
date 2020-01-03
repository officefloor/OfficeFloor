/*-
 * #%L
 * Team HTTP Server Tutorial
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.tutorial.teamhttpserver;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.HttpSessionStateful;

/**
 * Logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: values
@HttpSessionStateful
public class Template implements Serializable {
	private static final long serialVersionUID = 1L;

	private Map<Character, LetterEncryption> cache = new HashMap<Character, LetterEncryption>();

	private LetterEncryption displayCode;
	private String cacheThreadName;
	private String databaseThreadName;

	public LetterEncryption getTemplate() {
		return (this.displayCode == null ? new LetterEncryption(' ', ' ') : this.displayCode);
	}

	public Template getThreadNames() {
		return this;
	}

	public String getCacheThreadName() {
		return this.cacheThreadName;
	}

	public String getDatabaseThreadName() {
		return this.databaseThreadName;
	}
	// END SNIPPET: values

	// START SNIPPET: cache
	@FlowInterface
	public static interface PageFlows {
		void retrieveFromDatabase(char letter);
	}

	@Next("setDisplayCode")
	public LetterEncryption encrypt(EncryptLetter request, PageFlows flows) {

		// Specify thread name (clearing database thread)
		this.cacheThreadName = Thread.currentThread().getName();
		this.databaseThreadName = "[cached]";

		// Obtain from cache
		char letter = request.getLetter();
		LetterEncryption code = this.cache.get(Character.valueOf(letter));
		if (code != null) {
			return code;
		}

		// Not in cache so retrieve from database
		flows.retrieveFromDatabase(letter);
		return null; // for compiler
	}

	public void setDisplayCode(@Parameter LetterEncryption encryption) {
		this.displayCode = encryption;
	}
	// END SNIPPET: cache

	// START SNIPPET: database
	@Next("setDisplayCode")
	public LetterEncryption retrieveFromDatabase(@Parameter char letter, Connection connection) throws SQLException {

		// Specify thread name
		this.databaseThreadName = Thread.currentThread().getName();

		// Retrieve from database and cache
		PreparedStatement statement = connection.prepareStatement("SELECT CODE FROM LETTER_CODE WHERE LETTER = ?");
		statement.setString(1, String.valueOf(letter));
		ResultSet resultSet = statement.executeQuery();
		resultSet.next();
		String code = resultSet.getString("CODE");
		LetterEncryption letterCode = new LetterEncryption(letter, code.charAt(0));

		// Cache
		this.cache.put(Character.valueOf(letter), letterCode);

		return letterCode;
	}
	// END SNIPPET: database

	public void handleException(@Parameter Exception ex) throws Throwable {
		ex.printStackTrace();
		throw ex;
	}

}
