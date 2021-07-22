package net.officefloor.cabinet.dynamo;

import java.util.Optional;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

import net.officefloor.cabinet.OfficeCabinet;

/**
 * Dynamo DB {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoOfficeCabinet<F> implements OfficeCabinet<F> {

	/**
	 * {@link AmazonDynamoDB}.
	 */
	private final AmazonDynamoDB dynamoDb;

	/**
	 * Instantiate.
	 * 
	 * @param dynamoDb {@link AmazonDynamoDB}.
	 */
	public DynamoOfficeCabinet(AmazonDynamoDB dynamoDb) {
		this.dynamoDb = dynamoDb;
	}

	/*
	 * =================== OfficeCabinet ======================
	 */

	@Override
	public Optional<F> retrieveById(String id) {
		// TODO implement OfficeCabinet<F>.retrieveById
		throw new UnsupportedOperationException("TODO implement OfficeCabinet<F>.retrieveById");
	}

	@Override
	public void store(F entity) {
		// TODO implement OfficeCabinet<F>.store
		throw new UnsupportedOperationException("TODO implement OfficeCabinet<F>.store");
	}

}