package resources.elasticsearch;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.UUID;

import net.sf.json.util.JSONBuilder;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import static org.elasticsearch.common.xcontent.XContentFactory.*;

import resources.utils.VarUtils;

/**
 * this is an embedded elasticserach server
 * 
 * @author binyu
 * 
 */
public class EmbeddedESServer {

	private final Node node;
	private final String dataDirectory;

	public EmbeddedESServer() {
		this(VarUtils.ELASTICSEARCH_DATA);
	}

	public EmbeddedESServer(String dataDirectory) {
		this.dataDirectory = dataDirectory;

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
