package com.stackscaling.agentmaster.resources.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import com.stackscaling.agentmaster.resources.utils.IVirtualFileUtils;
import com.stackscaling.agentmaster.resources.utils.VarUtils;

/**
 * this is an embedded elasticserach server
 *
 * @author binyu
 *
 */
public class EmbeddedESServer {

	private final Node node;
	
	private final String dataDirectory;

	private IVirtualFileUtils vf = VarUtils.getVf(); 
	
	public EmbeddedESServer(String dataDirectory) {
		
		this.dataDirectory = vf.getRealFileFromRelativePath(dataDirectory).getAbsolutePath();

		ImmutableSettings.Builder elasticsearchSettings = ImmutableSettings
				.settingsBuilder()
				// .put("http.enabled", "false")
				.put("path.data", dataDirectory);

		node = new NodeBuilder().local(true)
				.settings(elasticsearchSettings.build())
				.data(true).node();

		// now add indices
		// for logs
//		HashMap<String, String> settings = new HashMap<String, String>();
//		settings.put("_ttl", VarUtils.ESLOG_DATA_TTL);
//		ImmutableSettings.Builder indicesSettings = ImmutableSettings.settingsBuilder().put(settings);
//
//		Client client = node.client();
//		if (!client.admin().indices().prepareExists("log").execute().actionGet().isExists()) {
//			client.admin().indices().prepareCreate("log").setSettings(indicesSettings).execute().actionGet();
//		}
//
//		// now set the type mappings
//		try {
//			XContentBuilder builder = jsonBuilder()
//					.startObject()
//					.startObject("CmdLog")
//					.startObject("properties")
//					.startObject("host").field("type", "string").endObject()
//					.startObject("httpStatusCode").field("type", "integer").endObject()
//					.startObject("timeReceived").field("type", "date").endObject()
//					.startObject("responseBody").field("type", "string").endObject()
//					.startObject("status").field("type", "string").endObject()
//					.endObject().endObject().endObject();
//
//			client.admin().indices().preparePutMapping("log")
//						.setType("CmdLog").setSource(builder).execute().actionGet();
//		} catch(IOException e) {
//			e.printStackTrace();
//		}

	}

	public Client getClient() {
		return node.client();
	}

	public void shutdown() {
		node.close();
		// deleteDataDirectory();
	}

	public String getDataDirectory() {
		return dataDirectory;
	}

	// private void deleteDataDirectory() {
	// try {
	// FileUtils.deleteDirectory(new File(dataDirectory));
	// } catch (IOException e) {
	// throw new
	// RuntimeException("Could not delete data directory of embedded elasticsearch server",
	// e);
	// }
	// }

}
