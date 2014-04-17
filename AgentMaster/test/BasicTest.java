import org.junit.Test;

import play.test.UnitTest;

public class BasicTest extends UnitTest {
	
	@Test
	public void testReplace() {
		System.out.println("{ \n test \n }".replaceAll("\n", "\\\\\n"));
	}

    @Test
    public void aVeryImportantThingToTest() {
        assertEquals(2, 1 + 1);
    }

}
