package com.stackscaling.agentmaster.resources.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stackscaling.agentmaster.resources.utils.IVirtualFileUtils;
import com.stackscaling.agentmaster.resources.utils.VarUtils;

/**
 * this is an embedded elasticserach server
 *
 * @author binyu
 *
 */
public class EmbeddedESServer {
	
	static Logger LOG = LoggerFactory.getLogger(EmbeddedESServer.class); 

	private final Node node;
	
	private final String dataDirectory;

	private IVirtualFileUtils vf = VarUtils.getVf(); 
	
	public EmbeddedESServer(String dataDirectory) {
		
		this.dataDirectory = vf.getRealFileFromRelativePath(dataDirectory).getAbsolutePath();
		LOG.info("embedded es engine using data dir = " + this.dataDirectory);
		
		ImmutableSettings.Builder elasticsearchSettings = ImmutableSettings
				.settingsBuilder()
				// .put("http.enabled", "false")
				.put("path.data", dataDirectory);

		node = new NodeBuilder().local(true)
				.settings(elasticsearchSettings.build())
				.data(true).node();

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
