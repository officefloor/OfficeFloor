/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.example.teamhttpserver;

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
import net.officefloor.plugin.web.http.template.HttpSessionStateful;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
@HttpSessionStateful
public class Template implements Serializable {

	@FlowInterface
	public static interface PageFlows {
		void retrieveFromDatabase(char letter);
	}

	private Map<Character, LetterCode> cache = new HashMap<Character, LetterCode>();

	private LetterCode displayCode;
	private String encodeThreadName;
	private String retrieveFromDatabaseThreadName;

	public LetterCode getTemplate() {
		return this.displayCode;
	}

	@NextTask("setDisplayCode")
	public LetterCode encode(EncodeLetter request, PageFlows flows) {

		// Specify thread name (clearing database)
		this.encodeThreadName = Thread.currentThread().getName();
		this.retrieveFromDatabaseThreadName = null;

		// Obtain from cache
		char letter = request.getLetter();
		LetterCode code = this.cache.get(new Character(letter));
		if (code != null) {
			return code;
		}

		// Not in cache so retrieve from database
		flows.retrieveFromDatabase(letter);
		return null; // for compiler
	}

	@NextTask("setDisplayCode")
	public LetterCode retrieveFromDatabase(@Parameter char letter,
			DataSource dataSource) throws SQLException {

		// Specify thread name
		this.retrieveFromDatabaseThreadName = Thread.currentThread().getName();

		Connection connection = dataSource.getConnection();
		try {
			PreparedStatement statement = connection
					.prepareStatement("SELECT CODE FROM LETTER_CODE WHERE LETTER = ?");
			statement.setString(1, String.valueOf(letter));
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			String code = resultSet.getString("CODE");
			LetterCode letterCode = new LetterCode(letter, code.charAt(0));

			this.cache.put(new Character(letter), letterCode);

			return letterCode;
		} finally {
			connection.close();
		}
	}

	@NextTask("getTemplate")
	public void setDisplayCode(@Parameter LetterCode code) {
		this.displayCode = code;
	}

	public Template getThreadNames() {
		return this;
	}

	public String getEncodeThreadName() {
		return this.encodeThreadName;
	}

	public String getRetrieveFromDatabaseThreadName() {
		return this.retrieveFromDatabaseThreadName;
	}

}