package resources.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.lightj.util.SpringContextUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import resources.utils.VarUtils;

@Configuration
public class EsResourceProvider {
	
	/**
	 * initialize elastic search embedded server
	 * @return
	 */
	public @Bean @Scope("singleton") @Lazy(true) EmbeddedESServer embeddedEsServer() {
		return new EmbeddedESServer();
	}

	/**
	 * elastic search client
	 * @return
	 */
	public @Bean @Scope("singleton") @Lazy(true) Client esClient() {
		if (VarUtils.LOCAL_ES_ENABLED) {
			EmbeddedESServer embededServer = SpringContextUtil.getBean("resources", EmbeddedESServer.class);
			return embededServer.getClient();
		}
		else {
			Client client = new TransportClient().addTransportAddress(
					new InetSocketTransportAddress(VarUtils.ELASTICSEARCH_EP, 9300));
			return client;
		}
	}
	

	/**
	 * get elastic search client
	 * @return
	 */
	public static Client getEsClient() {
		return SpringContextUtil.getBean("resources", Client.class);
	}
}
