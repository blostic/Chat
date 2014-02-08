package skibiak.server;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

public class ClientRequestHandlerTest {
	@Mock
	static private ClientConnectionAdapter client;
	@Mock
	static private ClientRequestHandler handler;
	@Mock
	static private Server server;

	@BeforeClass
	public static void setUp() {
		handler = new ClientRequestHandler(server, client);
	}

	@Test
	public void replaceQuotationTest() {
		Assert.assertEquals(
				handler.replaceQuotation("#createRoom -name \"Najlepsi szpiedzy\" -topic \"Kto jest najlepszym szpiegiem\" -type public"),
				"#createRoom -name \"Najlepsi#szpiedzy\" -topic \"Kto#jest#najlepszym#szpiegiem\" -type public");
	
		Assert.assertEquals(
				handler.replaceQuotation("#createRoom -topic \"Kto jest najlepszym szpiegiem\" -type public"),
				"#createRoom -topic \"Kto#jest#najlepszym#szpiegiem\" -type public");
	}

}
