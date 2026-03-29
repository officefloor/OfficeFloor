package net.officefloor.tutorial.httpservlet;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * {@link HttpServlet} for testing.
 * 
 * @author Daniel Sagenschneider
 */
@WebServlet("/servlet")
public class DecrementHttpServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/*
	 * =============== HttpServlet ========================
	 */

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// Read in content
		StringWriter buffer = new StringWriter();
		Reader entity = req.getReader();
		for (int character = entity.read(); character != -1; character = entity.read()) {
			buffer.write(character);
		}

		// Obtain value
		int value = Integer.parseInt(buffer.toString());

		// Return decremented value
		resp.getWriter().write(String.valueOf(value - 1));
	}

}
