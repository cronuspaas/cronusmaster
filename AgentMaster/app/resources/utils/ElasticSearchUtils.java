package resources.utils;

import java.util.Map;
import java.util.UUID;

import org.elasticsearch.client.Client;

import resources.elasticsearch.EsResourceProvider;

public class ElasticSearchUtils {
	
	/**
	 * insert a document 
	 * @param idx
	 * @param type
	 * @param id
	 * @param jsonDoc
	 */
	public static void insertDocument(String idx, String type, String id, String jsonDoc) {
		Client client = EsResourceProvider.getEsClient();

		// async add to index
		client.prepareIndex(idx, type)
				.setId(id)
				.setSource(jsonDoc).execute();
	}
	
	/**
	 * update an existing es doc
	 * @param idx
	 * @param type
	 * @param id
	 * @param jsonDoc
	 */
	public static void updateDocument(String idx, String type, String id, Map<String, String> values) {
		Client client = EsResourceProvider.getEsClient();
		client.prepareUpdate(idx, type, id)
				.setDoc(values).execute();
	}
	
	/**
	 * delete a document based on persisted cmdresponse indexmeta
	 * @param cmdResIndexMeta
	 */
	public static void deleteDocumentFromCmdResponse(String cmdResIndexMeta) {
		String[] tokens = cmdResIndexMeta.split("/", 3);
		if (tokens.length == 3) {
			EsResourceProvider.getEsClient()
						.prepareDelete(tokens[0], tokens[1], tokens[2])
						.execute();
		}
	}

}
