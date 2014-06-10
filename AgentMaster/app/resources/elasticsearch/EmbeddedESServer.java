package resources.elasticsearch;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import resources.utils.VarUtils;

/**
 * this is an embedded elasticserach server
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

		try {
			String uuid = UUID.randomUUID().toString();
			ImmutableSettings.Builder elasticsearchSettings = ImmutableSettings.settingsBuilder()
//                .put("http.enabled", "false")
					.put("node.name", uuid)
					.put("network.host", InetAddress.getLocalHost().getHostAddress())
			        .put("path.data", dataDirectory);

			node = new NodeBuilder()
            	.local(true)
            	.settings(elasticsearchSettings.build())
            	.clusterName(uuid)
            	.data(true)
            	.node();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

    }

    public Client getClient() {
        return node.client();
    }

    public void shutdown() {
        node.close();
//        deleteDataDirectory();
    }

	public String getDataDirectory() {
		return dataDirectory;
	}

//    private void deleteDataDirectory() {
//        try {
//            FileUtils.deleteDirectory(new File(dataDirectory));
//        } catch (IOException e) {
//            throw new RuntimeException("Could not delete data directory of embedded elasticsearch server", e);
//        }
//    }

}
