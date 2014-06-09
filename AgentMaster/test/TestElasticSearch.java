import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.Test;
import org.lightj.util.JsonUtil;
import org.lightj.util.SpringContextUtil;

import play.test.UnitTest;
import resources.UserDataProvider;
import resources.IUserDataDao.DataType;
import resources.elasticsearch.EmbeddedESServer;
import resources.elasticsearch.EsResourceProvider;
import resources.log.BaseLog.CommandResponse;
import resources.log.CmdLog;
import resources.log.IJobLogger;
import resources.log.ILog;
import resources.nodegroup.AdhocNodeGroupDataImpl;
import resources.utils.DataUtil;


public class TestElasticSearch extends UnitTest {
	
	@Test
	public void testEmbeddedEsServer() throws Exception {
		
//		SpringContextUtil.getBean("resources", EmbeddedESServer.class);
//		Client client = EsResourceProvider.getEsClient();
		
		Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("192.168.1.101", 9300));

		CmdLog jobLog = new CmdLog();
		Map<String, String> optionCleanup = new HashMap<String, String>();
		optionCleanup.put("key", "value");
		jobLog.setUserData(optionCleanup);
		jobLog.setCommandKey("testcmd");
		jobLog.setNodeGroup(AdhocNodeGroupDataImpl.NG_EMPTY);
		jobLog.setProgress(1000);
		jobLog.setStatus("Success");
		jobLog.setStatusDetail(100, 1, 1);
		CommandResponse commandResponse = new CommandResponse("testhost", 200, "some random string that can be very very very long");
		jobLog.addCommandResponse(commandResponse);
		String jsonStr = JsonUtil.encode(jobLog);

		IndexResponse response = client.prepareIndex("log", "cmdLog").setSource(jsonStr).execute().actionGet();
		// Index name
		String _index = response.getIndex();
		// Type name
		String _type = response.getType();
		// Document ID (generated or not)
		String _id = response.getId();
		// Version (if it's the first time you index this document, you will get: 1)
		long _version = response.getVersion();
		
		System.out.println(String.format("%s,%s,%s,%s", _index, _type, _id, _version));
		
		GetResponse queryRes = client.prepareGet("log", "cmdLog", _id)
		        .execute()
		        .actionGet();
		
		assertEquals(queryRes.getId(), _id);
		
	}
	
	public void testSearch() throws Exception
	{
//		Node node = new NodeBuilder().clusterName("test").client(true).node();
//		Client client = node.client();
//		
		
		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "test").build();
		Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("192.168.1.127", 9300));

		Map<String, Object> json = new HashMap<String, Object>();
		json.put("user","kimchy");
		json.put("postDate",new Date());
		json.put("message","trying out Elasticsearch");
		
		String jsonStr = JsonUtil.encode(json);
		
		IndexResponse response = client.prepareIndex("twitter", "tweet")
		        .setSource(jsonStr)
		        .execute()
		        .actionGet();
		
		// Index name
		String _index = response.getIndex();
		// Type name
		String _type = response.getType();
		// Document ID (generated or not)
		String _id = response.getId();
		// Version (if it's the first time you index this document, you will get: 1)
		long _version = response.getVersion();
		
		System.out.println(String.format("%s,%s,%s,%s", _index, _type, _id, _version));
		
		
		GetResponse queryRes = client.prepareGet("twitter", "tweet", "1")
		        .execute()
		        .actionGet();
		
//		DeleteResponse delRes = client.prepareDelete("twitter", "tweet", "1")
//		        .execute()
//		        .actionGet();
		
//		node.close();
	}
	

}
