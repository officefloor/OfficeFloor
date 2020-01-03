package net.officefloor.server.http.request.config;

/**
 * Configuration of a request/response communication.
 * 
 * @author Daniel Sagenschneider
 */
public class CommunicationConfig {

	/**
	 * {@link RequestConfig}.
	 */
	public RequestConfig request;

	public void setRequest(RequestConfig request) {
		this.request = request;
	}

	/**
	 * {@link ProcessConfig}.
	 */
	public ProcessConfig process;

	public void setProcess(ProcessConfig process) {
		this.process = process;
	}

	/**
	 * {@link ResponseConfig}.
	 */
	public ResponseConfig response;

	public void setResponse(ResponseConfig response) {
		this.response = response;
	}
}
