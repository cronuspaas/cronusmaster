import org.junit.Test;
import org.lightj.util.JsonUtil;
import org.lightj.util.MapListPrimitiveJsonParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.test.UnitTest;

public class BasicTest extends UnitTest {
	
	@Test
	public void testJson() throws Exception {
		String json = "{ " +
					"\"commands\" : [\"AGENT_CREATE_SERVICE\", \"AGENT_DELETE_SERVICE\"], " +
					"\"cmdUserData\" : {\"AGENT_CREATE_SERVICE\" : {\"serviceName\": \"myservice\"}, \"AGENT_DELETE_SERVICE\": {\"serviceName\": \"myservice\"}}" +
					"}";
		Object res = MapListPrimitiveJsonParser.parseJson(json);
		System.out.println(res);
	}
	
	@Test
	public void testReplace() {
		System.out.println("{ \n test \n }".replaceAll("\n", "\\\\\n"));
	}

    @Test
    public void aVeryImportantThingToTest() {
        assertEquals(2, 1 + 1);
    }

}
