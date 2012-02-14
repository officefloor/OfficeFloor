/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.tutorial.teamhttpserver;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.web.http.application.HttpSessionStateful;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: values
@HttpSessionStateful
public class Template implements Serializable {

	private Map<Character, LetterEncryption> cache = new HashMap<Character, LetterEncryption>();

	private LetterEncryption displayCode;
	private String cacheThreadName;
	private String databaseThreadName;

	public LetterEncryption getTemplate() {
		return this.displayCode;
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

	@NextTask("setDisplayCode")
	public LetterEncryption encrypt(EncryptLetter request, PageFlows flows) {

		// Specify thread name (clearing database thread)
		this.cacheThreadName = Thread.currentThread().getName();
		this.databaseThreadName = "[cached]";

		// Obtain from cache
		char letter = request.getLetter();
		LetterEncryption code = this.cache.get(new Character(letter));
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
	@NextTask("setDisplayCode")
	public LetterEncryption retrieveFromDatabase(@Parameter char letter,
			DataSource dataSource) throws SQLException {

		// Specify thread name
		this.databaseThreadName = Thread.currentThread().getName();

		// Retrieve from database and cache
		Connection connection = dataSource.getConnection();
		try {
			PreparedStatement statement = connection
					.prepareStatement("SELECT CODE FROM LETTER_CODE WHERE LETTER = ?");
			statement.setString(1, String.valueOf(letter));
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			String code = resultSet.getString("CODE");
			LetterEncryption letterCode = new LetterEncryption(letter,
					code.charAt(0));

			// Cache
			this.cache.put(new Character(letter), letterCode);

			return letterCode;
		} finally {
			connection.close();
		}
	}
	// END SNIPPET: database

}