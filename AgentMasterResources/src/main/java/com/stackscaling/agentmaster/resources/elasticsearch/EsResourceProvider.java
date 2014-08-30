package com.stackscaling.agentmaster.resources.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.lightj.util.SpringContextUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import com.stackscaling.agentmaster.resources.utils.VarUtils;

@Configuration
public class EsResourceProvider {

	private static Client esClient;

	/**
	 * initialize elastic search embedded server
	 * @return
	 */
	public @Bean @Scope("singleton") @Lazy(true) EmbeddedESServer embeddedEsServer() {
		return new EmbeddedESServer(VarUtils.esDataPath);
	}

	/**
	 * elastic search client
	 * @return
	 */
	public @Bean @Scope("singleton") @Lazy(true) Client esClient() {
		if (VarUtils.isLocalEsEnabled) {
			return getEmbeddedEsServer().getClient();
		}
		else {
			Client client = new TransportClient().addTransportAddress(
					new InetSocketTransportAddress(VarUtils.esEp, 9300));
			return client;
		}
	}

	/**
	 * get embedded elastic search server
	 * @return
	 */
	public static EmbeddedESServer getEmbeddedEsServer() {
		return SpringContextUtil.getBean("resources", "embeddedEsServer", EmbeddedESServer.class);
	}


	/**
	 * get elastic search client
	 * @return
	 */
	public synchronized static Client getEsClient() {
		if (esClient == null) {
			if (VarUtils.isLocalEsEnabled) {
				esClient = getEmbeddedEsServer().getClient();
			}
			else {
				esClient = new TransportClient().addTransportAddress(
						new InetSocketTransportAddress(VarUtils.esEp, 9300));
			}
		}
		return esClient;
	}
}
