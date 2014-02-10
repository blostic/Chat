package skibiak.tests;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import skibiak.server.CensoredRoom;
import skibiak.server.ClientConnectionAdapter;
import skibiak.server.Server;

public class CensoredRoomTest {

	private Server server = mock(Server.class);
	private ClientConnectionAdapter client = mock(ClientConnectionAdapter.class);
	
	private CensoredRoom room;

	@Before
	public void setUp() {
		when(server.isActive()).thenReturn(true);
		room = new CensoredRoom(server, "test room", "test topic");
	}

	@Test
	public void censureMessageTest() {
		Assert.assertEquals(
				"Jules: [while cleaning the bloodied car] Oh man, "
						+ "I will never forgive your *** for this ****. "
						+ "This is some ****** up repugnant ****. "
						+ "Vincent: Jules, did you ever hear the philosophy that "
						+ "once a man admits that he is wrong, that he is immediately "
						+ "forgiven for all wrongdoings? Have you ever heard that?",
				room.censureMessage("Jules: [while cleaning the bloodied car] Oh man, "
						+ "I will never forgive your ass for this shit. "
						+ "This is some fucked up repugnant shit. "
						+ "Vincent: Jules, did you ever hear the philosophy that "
						+ "once a man admits that he is wrong, that he is immediately "
						+ "forgiven for all wrongdoings? Have you ever heard that?"));
	}
	
	@Test(expected = IOException.class)
	public void addBannedWordsTest() throws IOException {
		room.addBannedWords("NoSuchFile");
	}
	
	@Test
	public void readAndAnnounceMessageTest() throws IOException{
		room.readMessages();
		room.addClient(client);
		room.readMessages();
		
		when(client.containMessage()).thenReturn(true);
		when(client.readMessage()).thenReturn("Some Message");
		
		room.readMessages();
		
		when(client.readMessage()).thenReturn("#test");
		room.readMessages();
		
		when(client.readMessage()).thenReturn("#test -strange option");
		room.readMessages();
		
		when(server.isActive()).thenReturn(false);
	}
	
}
