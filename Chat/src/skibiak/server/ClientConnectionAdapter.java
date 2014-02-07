package skibiak.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnectionAdapter {
	private String nickname;
	private Room presentRoom;
	private PrintWriter out;
	private BufferedReader in;

	public Room getPresentRoom() {
		return presentRoom;
	}

	public void setPresentRoom(Room presentRoom) {
		this.presentRoom.removeClient(this);
		this.presentRoom = presentRoom;
		presentRoom.clients.add(this);
	}

	public ClientConnectionAdapter(Socket socket, Room presetRoom) {
		this.presentRoom = presetRoom;
		try {
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
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
