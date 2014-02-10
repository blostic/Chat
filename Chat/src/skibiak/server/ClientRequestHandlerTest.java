package skibiak.server;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.beust.jcommander.ParameterException;


public class ClientRequestHandlerTest {
	static private ClientConnectionAdapter client = mock(ClientConnectionAdapter.class);
	static private ClientRequestHandler handler = mock(ClientRequestHandler.class);
	static private Server server = mock(Server.class);
	static private Room room = mock(PublicRoom.class);
	
	@BeforeClass
	public static void setUp() {
		handler = new ClientRequestHandler(server, client);
		when(client.getUsername()).thenReturn("Banek");
		when(room.getChatTopic()).thenReturn("Chat Topic");
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
	
	@Test(expected=ParameterException.class)
	public void executeCommandExcetpionTest(){
		handler.executeCommand("should throw error");
	}
	
	@Test
	public void helpTest(){
		handler.executeCommand("#help");
	}
	
	@Test
	public void exitTest(){
		handler.executeCommand("#exit");
	}
	
	@Test
	public void createRoomTest(){
		handler.executeCommand("#createRoom -name \"Some room\" -topic \"some topic\" -type public");
		handler.executeCommand("#createRoom -name \"\" -topic \"some topic\" -type p");		
		handler.executeCommand("#createRoom -name \"\" -topic \"some topic\" -type public");
	}
	
//	@Test
//	public void changeRoomTopic(){
//		handler.executeCommand("#createRoom -name \"\" -topic \"some topic\" -type public");
//	}

}
