package skibiak.tests;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import skibiak.server.ClientConnectionAdapter;
import skibiak.server.Room;
import skibiak.server.Server;

public class ServerTest {
	
	static private Room room = mock(Room.class);
	static private ClientConnectionAdapter client = mock(ClientConnectionAdapter.class);
	static private ClientConnectionAdapter client2 = mock(ClientConnectionAdapter.class);
	
	
	private static Server server;
	
	@Before
	public void setUp() throws IOException{
		server = new Server(4002);		
	}

	@After
    public void tearDown() throws IOException {
        server.closeServer();
    }
	
	@Test
	public void runServerTest() throws InterruptedException{
		new Thread(server).start();
	}

	@Test
	public void addRoomToServerTest(){
		when(room.getRoomName()).thenReturn("Teens");
		
		Assert.assertTrue(server.addRoomToServer(room));
		Assert.assertEquals(server.getRooms().get("Teens").getRoomName(),"Teens");
		Assert.assertFalse(server.addRoomToServer(room));
	}
	
	@Test
	public void addUserToRoomTest(){
		
		when(client2.getUsername()).thenReturn("TestUser");
		server.addUserToRoom(client2, "MainRoom");
		server.addUserToRoom(client2, "MainRoo2");
		
		
		Assert.assertTrue(server.containUser("TestUser"));
		Assert.assertFalse(server.containUser("Test"));
		
	}
	
	@Test
	public void serverPlusClientTest() throws UnknownHostException, IOException, InterruptedException {
		new Thread(server).start();
		try (	Socket socket = new Socket("localhost", 4002);
				Socket socket2 = new Socket("localhost", 4002)) {

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream());			
			Assert.assertTrue(in.readLine().equals(">Please choose a username: "));
			out.write("Benek\n");
			out.flush();
			Assert.assertTrue(in.readLine().equals(">Welcome Benek!"));
			
			BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
			PrintWriter out2 = new PrintWriter(socket2.getOutputStream());			
			Assert.assertTrue(in2.readLine().equals(">Please choose a username: "));
			out2.write("Benek\n");
			out2.flush();

			Assert.assertTrue(in2.readLine().equals(">Username Benek is already in use. Try something else."));
			out2.write("Benn\n");
			out2.flush();
			
			Assert.assertTrue(in2.readLine().equals(">Welcome Benn!"));
			Assert.assertTrue(in.readLine().equals(">User Benn entered the room"));
			
			in.close();
			in2.close();
			out.close();
			out2.close();
		}
		server.closeServer();
	}
	
}
