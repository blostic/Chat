package skibiak.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Server {
	private static Logger logger = Logger.getLogger(Server.class);
	private int port;
	private Map<String, Room> rooms;

	public Map<String, Room> getRooms() {
		return rooms;
	}

	public Server(int port) throws ClassNotFoundException {
		this.port = port;
		this.rooms = Collections.synchronizedMap(new HashMap<String, Room>());
		rooms.put("MainRoom", RoomFactory.getInstance(this, "MainRoom",
				"PublicRoom", "Everything about Everyone", "bb"));
	}

	public void runServer() throws IOException {
		while (true) {
			try (ServerSocket socket = new ServerSocket(this.port)) {
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

	public void addUserToRoom(ClientConnectionAdapter client, String roomName) {
		if (rooms.containsKey(roomName)) {
			Room room = rooms.get(roomName);;
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
			try {
				Boolean nickFree = false;
				client.sendMessage("Please choose a nickname: ");
				String nickname = "";
				while (!nickFree) {
					nickFree = true;
					nickname = client.readMessage();
					if (nickname.length() > 3) {
						for (Room room : Server.this.rooms.values()) {
							for (ClientConnectionAdapter clientConnection : room.clients) {
								if (clientConnection.getNickname().equals(
										nickname)) {
									nickFree = false;
									client.sendMessage("Sorry, nickname "
											+ nickname
											+ " is already in use. Try something else.");
									break;
								}
							}
							if (!nickFree) {
								break;
							}
						}
					} else {
						nickFree = false;
						client.sendMessage("Sorry, nickname must have at least 4 letters");
					}
				}
				client.setNickname(nickname);
				client.sendMessage("Welcome " + nickname + "!");
				logger.info("User" + nickname + " has logged");
			} catch (IOException e) {
				e.printStackTrace();
			}

			Server.this.rooms.get("MainRoom").addClient(client);
		}
	}

}
