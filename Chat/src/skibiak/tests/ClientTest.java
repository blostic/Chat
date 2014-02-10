package skibiak.tests;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Assert;
import org.junit.Test;

import skibiak.client.Client;

public class ClientTest {

	private Client client;
	private Socket clientSocket;

	private PrintWriter out;

	@Test
	public void BangTest() throws IOException, InterruptedException {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try (ServerSocket socket = new ServerSocket(4001)) {
					clientSocket = socket.accept();
					out = new PrintWriter(clientSocket.getOutputStream());
					
					out.write("Proba\n");
					out.flush();

					out.write(">Welcome Berta\n");
					out.flush();
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}).start();

		String data = "Berta\n#exit\n";
		InputStream old = System.in;
		InputStream testInput = new ByteArrayInputStream(data.getBytes("UTF-8"));
		System.setIn(testInput);
		
		client = new Client(4001, "localhost");		
		Thread.sleep(100);
		
		client.startClient();
		client.setActive(false);
		Assert.assertTrue(client.getNickname().equals("Berta"));
		System.setIn(old);
		
		testInput.close();
		clientSocket.close();
		out.close();
	}

}
