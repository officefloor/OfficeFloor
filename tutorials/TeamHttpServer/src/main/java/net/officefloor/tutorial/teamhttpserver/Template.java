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
@HttpSessionStateful // caches this object in session
public class Template implements Serializable {

	// Session cache
	private Map<Character, LetterEncryption> cache = new HashMap<Character, LetterEncryption>();

	// Template properties
	private LetterEncryption displayCode;
	private String cacheThreadName;
	private String databaseThreadName;
	
	public String getCacheThreadName() {
		return this.cacheThreadName;
	}
	
	public String getDatabaseThreadName() {
		return this.databaseThreadName;
	}

	
	// Template sections

	public LetterEncryption getTemplate() {
		return (this.displayCode == null ? new LetterEncryption(' ', ' ') : this.displayCode);
	}

	public Template getThreadNames() {
		return this;
	}
	// END SNIPPET: values

	private static final long serialVersionUID = 1L;

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