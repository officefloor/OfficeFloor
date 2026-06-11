package net.officefloor.tutorial.teamhttpserver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;

public class EncryptLogic {

	@FlowInterface
	public interface PageFlows {
		void retrieveFromDatabase(char letter);
	}

	@Next("setDisplayCode")
	public LetterEncryption encrypt(Template template, EncryptLetter request, PageFlows flows) {
		template.setCacheThreadName(Thread.currentThread().getName());
		template.setDatabaseThreadName("[cached]");

		char letter = request.getLetter();
		LetterEncryption code = template.getCache().get(letter);
		if (code != null) {
			return code;
		}

		flows.retrieveFromDatabase(letter);
		return null;
	}

	@Next("setDisplayCode")
	public LetterEncryption retrieveFromDatabase(Template template, @Parameter char letter, Connection connection)
			throws SQLException {
		template.setDatabaseThreadName(Thread.currentThread().getName());

		PreparedStatement statement = connection.prepareStatement("SELECT CODE FROM LETTER_CODE WHERE LETTER = ?");
		statement.setString(1, String.valueOf(letter));
		ResultSet resultSet = statement.executeQuery();
		resultSet.next();
		String code = resultSet.getString("CODE");
		LetterEncryption letterCode = new LetterEncryption(letter, code.charAt(0));

		template.getCache().put(letter, letterCode);
		return letterCode;
	}

	public void setDisplayCode(Template template, @Parameter LetterEncryption encryption,
			ServerHttpConnection connection) throws IOException {
		template.setDisplayCode(encryption);
		connection.getResponse().setStatus(HttpStatus.SEE_OTHER);
		connection.getResponse().getHeaders().addHeader("location", "/example");
	}
}
