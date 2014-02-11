package skibiak.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnectionAdapter {
	private String username;
	private Room presentRoom;
	private final PrintWriter out;
	private final BufferedReader in;

	public Room getPresentRoom() {
		return presentRoom;
	}

	public void setPresentRoom(Room presentRoom) {
		this.presentRoom.removeClient(this);
		this.presentRoom = presentRoom;
	}

	public ClientConnectionAdapter(Socket socket, Room presetRoom)
			throws IOException {
		this.presentRoom = presetRoom;
		out = new PrintWriter(socket.getOutputStream());
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public void sendMessage(String message) {
		out.write(message + "\n");
		out.flush();
	}

	public String readMessage() throws IOException {
		return in.readLine();
	}

	public Boolean containMessage() throws IOException {
		return in.ready();
	}

	public Boolean clientDisconected() {
		return out.checkError();
	}
}
