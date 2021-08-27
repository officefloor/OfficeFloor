package net.officefloor.cabinet.cosmosdb;

import java.util.Arrays;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.PartitionKeyDefinition;

import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.AbstractDocumentMetaData;
import net.officefloor.cabinet.common.CabinetUtil;

/**
 * Meta-data for the {@link CosmosOfficeCabinet} {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
class CosmosDocumentMetaData<D> extends AbstractDocumentMetaData<InternalObjectNode, InternalObjectNode, D> {

	/**
	 * {@link CosmosContainer}.
	 */
	final CosmosContainer container;

	/**
	 * Instantiate.
	 * 
	 * @param documentType   Document type.
	 * @param cosmosDatabase {@link CosmosDatabase}.
	 * @throws Exception If fails to instantiate {@link OfficeCabinet}.
	 */
	CosmosDocumentMetaData(Class<D> documentType, CosmosDatabase cosmosDatabase) throws Exception {
		super(documentType);

		// Obtain the container id
		String containerId = CabinetUtil.getDocumentName(documentType);

		// Create the container
		CosmosContainerProperties createContainer = new CosmosContainerProperties(containerId,
				new PartitionKeyDefinition().setPaths(Arrays.asList("/id")));
		cosmosDatabase.createContainerIfNotExists(createContainer);

		// Obtain the container
		this.container = cosmosDatabase.getContainer(documentType.getSimpleName());
	}

	@Override
	protected void initialise(Initialise<InternalObjectNode, InternalObjectNode> init) throws Exception {

		// Internal document
		init.setInternalDocumentFactory(() -> new InternalObjectNode());

		// Keys
		init.setKeyGetter((internalObjectNode, keyName) -> internalObjectNode.getId());
		init.setKeySetter((internalObjectNode, keyName, keyValue) -> internalObjectNode.setId(keyValue));

		// Primitives
		init.addFieldType(boolean.class, Boolean.class, InternalObjectNode::getBoolean, InternalObjectNode::set);
		init.addFieldType(byte.class, Byte.class, (doc, property) -> {
			Integer value = doc.getInt(property);
			return value != null ? value.byteValue() : null;
		}, (doc, property, value) -> doc.set(property, value != null ? Integer.valueOf(value) : null));
		init.addFieldType(short.class, Short.class, (doc, property) -> {
			Integer value = doc.getInt(property);
			return value != null ? value.shortValue() : null;
		}, (doc, property, value) -> doc.set(property, value != null ? Integer.valueOf(value) : null));
		init.addFieldType(char.class, Character.class, (item, attributeName) -> {
			String value = item.getString(attributeName);
			return value != null ? value.charAt(0) : null;
		}, (doc, property, value) -> doc.set(property, value != null ? new String(new char[] { value }) : null));
		init.addFieldType(int.class, Integer.class, InternalObjectNode::getInt, InternalObjectNode::set);
		init.addFieldType(long.class, Long.class, (doc, property) -> {
			String value = doc.getString(property);
			return value != null ? Long.parseLong(value) : null;
		}, (doc, property, value) -> {
			String text = value != null ? String.valueOf(value) : null;
			doc.set(property, text);
		});
		init.addFieldType(float.class, Float.class, (doc, property) -> {
			Double value = doc.getDouble(property);
			return value != null ? value.floatValue() : null;
		}, (doc, property, value) -> doc.set(property, value != null ? Double.valueOf(value) : null));
		init.addFieldType(double.class, Double.class, InternalObjectNode::getDouble, InternalObjectNode::set);

		// Open types
		init.addFieldType(String.class, InternalObjectNode::getString, InternalObjectNode::set);
	}

}
