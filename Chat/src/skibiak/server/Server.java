package skibiak.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Server {
	private static Logger logger = Logger.getLogger(Server.class);
	private Map<String, Room> rooms;
	private int port;

	public Map<String, Room> getRooms() {
		return rooms;
	}

	public Server(int port) throws ClassNotFoundException {
		this.port = port;
		this.rooms = Collections.synchronizedMap(new HashMap<String, Room>());
		rooms.put("MainRoom", RoomFactory.getInstance(this, "MainRoom",
				"public", "Everything about Everyone", "bb"));
	}

	public void runServer() throws IOException {
		try (ServerSocket socket = new ServerSocket(port)) {
			while (true) {
				Socket clientSocket = socket.accept();
				new EstablishUser(clientSocket);
			}
		}
	}

	public void addRoomToServer(ClientConnectionAdapter client, Room room) {
		String roomName = room.getRoomName();
		if (!rooms.containsKey(roomName)) {
			rooms.put(room.getRoomName(), room);
			client.sendMessage("Created room " + roomName);
		} else {
			logger.warn("User " + client.getNickname()
					+ " tried to add room which already exist [" + roomName
					+ "]");
			client.sendMessage("Room already exist");
		}
	}

	public boolean containUser(String nickname) {
		for (Room room : rooms.values()) {
			for (ClientConnectionAdapter clientConnection : room.clients) {
				if (clientConnection.getNickname().equals(nickname)) {
					return true;
				}
			}
		}
		return false;
	}

	public void addUserToRoom(ClientConnectionAdapter client, String roomName) {
		if (rooms.containsKey(roomName)) {
			Room room = rooms.get(roomName);
			client.setPresentRoom(room);
			logger.info("User " + client.getNickname() + " added to room "
					+ roomName);
		} else {
			logger.warn("User " + client.getNickname()
					+ " has selected a room that does not exist [" + roomName
					+ "]");
			client.sendMessage("Room doesn't exist");
		}
	}

	public void removeUnresponsiveClients() {
		for (Room room : rooms.values()) {
			List<ClientConnectionAdapter> clientsToRemove = new ArrayList<ClientConnectionAdapter>();
			for (ClientConnectionAdapter clientConnection : room.clients) {
				if (clientConnection.clientDisconected()) {
					clientsToRemove.add(clientConnection);
				}
			}
			room.clients.removeAll(clientsToRemove);
		}
	}
	
	public static void main(String[] args) {
		try {
			PropertyConfigurator.configure("log4j.properties");
			logger.setLevel(Level.INFO);

			int port = 4000;
			logger.info("Server is listening at port " + port);
			new Server(port).runServer();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private class EstablishUser extends Thread {
		private Socket clientSocket;

		private EstablishUser(Socket clientSocket) {
			this.clientSocket = clientSocket;
			this.start();
		}

		@Override
		public void run() {
			ClientConnectionAdapter client = new ClientConnectionAdapter(
					clientSocket, rooms.get("MainRoom"));
			Boolean nickFree = false;
			client.sendMessage("Please choose a nickname: ");
			String nickname = "";
			try {
				while (!nickFree) {
					nickname = client.readMessage();
					if (nickname.length() > 3) {
						nickFree = !containUser(nickname);
					} else {
						nickFree = false;
						client.sendMessage("Nickname must have at least 4 letters");
					}
					if (nickFree) {
						synchronized (rooms.get("MainRoom")) {
							if (!containUser(nickname)) {
								client.setNickname(nickname);
								client.sendMessage("Welcome " + nickname + "!");
								logger.info("User" + nickname + " has logged");
								rooms.get("MainRoom").addClient(client);
							}
						}
					} else {
						client.sendMessage("Nickname " + nickname
								+ " is already in use. Try something else.");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("User left the chat before choosing nickname");
			}
		}
	}

}
