package skibiak.tests;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import skibiak.server.ClientConnectionAdapter;
import skibiak.server.ClientRequestHandler;
import skibiak.server.Room;
import skibiak.server.RoomFactory;
import skibiak.server.Server;

import com.beust.jcommander.ParameterException;


public class ClientRequestHandlerTest {
	private static ClientConnectionAdapter client = mock(ClientConnectionAdapter.class);
	private static Room room = mock(Room.class);
	private Room realRoom;
	private static Server server;
	
	private ClientRequestHandler handler; 

	@BeforeClass
	public static void setUpMocks() throws IOException{
		when(client.getUsername()).thenReturn("Benek");
		when(room.getChatTopic()).thenReturn("Chat Topic");
		server = new Server(4000);
	}

	@Before
	public void setUp () throws ClassNotFoundException{
		realRoom = RoomFactory.getInstance(server, "Main", "public", "notImportant");
		when(client.getPresentRoom()).thenReturn(realRoom);
		handler = new ClientRequestHandler(server, client);
	}
	
	@Test
	public void replaceQuotationTest() throws NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Method m = ClientRequestHandler.class.getMethod("replaceQuotation", String.class);
		Assert.assertEquals(
				m.invoke(handler, "#createRoom"
						+ " -name \"Najlepsi szpiedzy\""
						+ " -topic \"Kto jest najlepszym szpiegiem\""
						+ " -type public"),
				"#createRoom -name \"Najlepsi#szpiedzy\""
						+ " -topic \"Kto#jest#najlepszym#szpiegiem\""
						+ " -type public");
		Assert.assertEquals(m.invoke(handler, "#createRoom -topic"
				+ " \"Kto jest najlepszym szpiegiem\" " + "-type public"),
				"#createRoom -topic \"Kto#jest#najlepszym#szpiegiem\" "
						+ "-type public");
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
		handler.executeCommand("#createRoom -topic \"some topic\"");
		server.addRoomToServer(room);
		when(room.getRoomName()).thenReturn("Some room");
		Assert.assertFalse(server.addRoomToServer(room));
		handler.executeCommand("#createRoom -name \"\" -topic \"some topic\" -type public");		
	}
	
	@Test
	public void changeRoomTopic(){
		handler.executeCommand("#changeTopic -topic \"Some new Topic\"");
		Assert.assertEquals("Some new Topic",realRoom.getChatTopic());
	}

}
