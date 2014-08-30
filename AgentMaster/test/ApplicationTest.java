import org.junit.Test;

import com.stackscaling.agentmaster.resources.utils.VarUtils;

import play.libs.Crypto;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

public class ApplicationTest extends FunctionalTest {

    @Test
    public void testThatIndexPageWorks() {
        Response response = GET("/");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
        System.out.println(Crypto.encryptAES("mypassword"));
        System.out.println(VarUtils.agentPassword);
        System.out.println(VarUtils.agentPasswordBase64);
    }
    
}