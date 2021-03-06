package test.java.skibiak;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import main.java.skibiak.server.ClientConnectionAdapter;
import main.java.skibiak.server.PublicRoom;
import main.java.skibiak.server.Server;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PublicRoomTest {
	
	private Server server = mock(Server.class);
	private ClientConnectionAdapter client = mock(ClientConnectionAdapter.class);
	
	private PublicRoom room;

	@Before
	public void setUp() {
		when(server.isActive()).thenReturn(true);
		room = new PublicRoom(server, "test room", "test topic");
	}

	@Test
	public void readAndAnnounceMessageTest() throws IOException{
		room.handleClients();
		room.addClient(client);
		room.handleClients();
		
		when(client.containMessage()).thenReturn(true);
		when(client.readMessage()).thenReturn("Some Message");
		
		room.handleClients();
		
		when(client.readMessage()).thenReturn("#test");
		room.handleClients();
		
		when(client.readMessage()).thenReturn("#test -strange option");
		room.handleClients();
		
		when(server.isActive()).thenReturn(false);
	}
	
	@Test
	public void removeClient(){
		when(client.getUsername()).thenReturn("Benek");
		
		room.addClient(client);
		Assert.assertTrue(room.containUser(client.getUsername()));
		
		room.removeClient(client);
		Assert.assertFalse(room.containUser(client.getUsername()));
	}
	
	@Test
	public void removeUnresponsiveClients(){
		when(client.getUsername()).thenReturn("Benek");
		room.addClient(client);
		
		when(client.clientDisconected()).thenReturn(false);
		room.removeUnresponsiveClients();
		Assert.assertTrue(room.containUser(client.getUsername()));
		
		when(client.clientDisconected()).thenReturn(true);
		room.removeUnresponsiveClients();
		Assert.assertFalse(room.containUser(client.getUsername()));
	}
	
	@Test
	public void setChatTopicTest(){
		room.setChatTopic("Random topic");
		Assert.assertEquals("Random topic", room.getChatTopic());
	}

}
