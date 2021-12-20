package net.officefloor.cabinet.domain;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.Query;
import net.officefloor.cabinet.spi.Query.QueryField;

/**
 * {@link MethodImplementation} to retrieve a {@link Document} by {@link Query}.
 * 
 * @author Daniel Sagenschneider
 */
public class RetrieveByQueryMethodImplementation<D> implements MethodImplementation {

	private final Class<D> documentType;

	private final String[] queryFieldNames;

	public RetrieveByQueryMethodImplementation(Class<D> documentType, String[] queryFieldNames) {
		this.documentType = documentType;
		this.queryFieldNames = queryFieldNames;
	}

	/*
	 * ======================= MethodImplementation =======================
	 */

	@Override
	public Object invoke(CabinetSession session, Object[] arguments) throws Exception {

		// Obtain the office cabinet
		OfficeCabinet<D> cabinet = session.getOfficeCabinet(this.documentType);

		// Create the query
		QueryField[] queryFields = new QueryField[arguments.length];
		for (int i = 0; i < queryFields.length; i++) {
			String queryFieldName = this.queryFieldNames[i];
			Object queryValue = arguments[i];
			queryFields[i] = new QueryField(queryFieldName, queryValue);
		}

		// Execute the query
		return cabinet.retrieveByQuery(new Query(queryFields));
	}

}
