package skibiak.tests;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    public void tearDown() throws IOException, InterruptedException {
        server.closeServer();
        Thread.sleep(50);
    }
	
	@Test
	public void runServerTest() throws UnknownHostException, IOException{
		new Thread(server).start();
		Socket socket = new Socket("localhost",4002);
		Assert.assertTrue(socket.isConnected());
		socket.close();
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
		when(client.getUsername()).thenReturn("TestUser");
		when(client2.getUsername()).thenReturn("TestUser2");
		Assert.assertTrue(server.addUserToRoom(client, "MainRoom"));
		Assert.assertFalse(server.addUserToRoom(client2, "MainRoo2"));
		
		Assert.assertTrue(server.containsUser("TestUser"));
		Assert.assertFalse(server.containsUser("TestUser2"));
		
	}
	
	@Test
	public void incorectNicknameTest() throws UnknownHostException, IOException {
		new Thread(server).start();
		try (Socket socket = new Socket("localhost", 4002)) {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			Assert.assertTrue(in.readLine().equals(
					">Please choose a username: "));
			out.write("Be\n");
			out.flush();
			Assert.assertTrue(in.readLine().equals(
					">Valid login consists of 3 to 15 "
							+ "alphanumeric (plus _-) symbols"));
			out.write("Be!@#!#%@#$!\n");
			out.flush();
			Assert.assertTrue(in.readLine().equals(
					">Valid login consists of 3 to 15 "
							+ "alphanumeric (plus _-) symbols"));
		}
	}
	
	@Test
	public void repeatedNicknameTest() throws UnknownHostException, IOException {
		new Thread(server).start();
		try (	Socket socket = new Socket("localhost", 4002);
				Socket socket2 = new Socket("localhost", 4002)) {

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			Assert.assertTrue(in.readLine().equals(">Please choose a username: "));
			out.write("Benek\n");
			out.flush();
			Assert.assertTrue(in.readLine().equals(">Welcome Benek!"));
			Assert.assertTrue(in.readLine()
					.equals(">Type #help to familiarize yourself with the chat capabilities"));

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
	}
	
	@Test
	public void serverControllerTest() throws InterruptedException, IOException{
		new Thread(server).start();
		String data = "noteExactlyWhatWeWant\n#exit\n";
		InputStream old = System.in;
		InputStream testInput = new ByteArrayInputStream(data.getBytes("UTF-8"));
		System.setIn(testInput);
		Thread.sleep(200);
		System.setIn(old);
		Assert.assertFalse(server.isActive());
	}
}
