package skibiak.tests;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import skibiak.server.ClientConnectionAdapter;
import skibiak.server.ClientRequestHandler;
import skibiak.server.PublicRoom;
import skibiak.server.Room;
import skibiak.server.Server;

import com.beust.jcommander.ParameterException;


public class ClientRequestHandlerTest {
	static private ClientConnectionAdapter client = mock(ClientConnectionAdapter.class);
	static private Server server = mock(Server.class);
	static private Room room = mock(PublicRoom.class);
	
	private ClientRequestHandler handler; 

	@BeforeClass
	public static void setUpMocks() {
		when(client.getUsername()).thenReturn("Banek");
		when(client.getPresentRoom()).thenReturn(room);
		when(room.getChatTopic()).thenReturn("Chat Topic");
	}

	@Before
	public void setUp (){
		handler = new ClientRequestHandler(server, client);		
	}
	
	@Test
	public void replaceQuotationTest() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method m = ClientRequestHandler.class.getMethod("replaceQuotation",String.class);
	
		Assert.assertEquals( m.invoke(handler, "#createRoom -name \"Najlepsi szpiedzy\" -topic \"Kto jest najlepszym szpiegiem\" -type public"),
				"#createRoom -name \"Najlepsi#szpiedzy\" -topic \"Kto#jest#najlepszym#szpiegiem\" -type public");
	
		Assert.assertEquals( m.invoke(handler, "#createRoom -topic \"Kto jest najlepszym szpiegiem\" -type public"),
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
