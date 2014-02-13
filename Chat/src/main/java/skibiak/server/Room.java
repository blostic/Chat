package main.java.skibiak.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.beust.jcommander.ParameterException;

public abstract class Room implements Runnable {
	private final String roomName;
	private String chatTopic;

	private final List<ClientConnectionAdapter> clients;
	protected final Server server;
	protected static Logger logger = Logger.getLogger(Room.class);

	public Room(Server server, String roomName, String chatTopic) {
		this.clients = new CopyOnWriteArrayList<ClientConnectionAdapter>();
		this.chatTopic = chatTopic;
		this.roomName = roomName;
		this.server = server;

		PropertyConfigurator.configure("main/resources/log4j.properties");
		logger.setLevel(Level.INFO);
	}

	public abstract void annouceMessage(String message);

	public void startRoom() {
		new Thread(this).start();
		new Runnable() {

			@Override
			public void run() {
				while (server.isActive()) {
					try {
						Thread.sleep(100);
						removeUnresponsiveClients();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
	}

	public void run() {
		while (server.isActive()) {
			try {
				Thread.sleep(10);
				handleClients();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void handleClients() throws IOException {
		for (ClientConnectionAdapter connection : this.getClients()) {
			if (connection.containMessage()) {
				String message = connection.readMessage();
				if (!message.startsWith("#")) {
					annouceMessage(connection.getUsername() + ": " + message);
				} else {
					try {
						new ClientRequestHandler(server, connection)
								.executeCommand(message);
					} catch (ParameterException e) {
						connection.sendMessage(">No such option, "
								+ "use #help to check correct syntax");
					}
				}
			}
		}
	}

	public boolean containUser(String username){
		for (ClientConnectionAdapter clientConnection : this.getClients()) {
			if (clientConnection.getUsername().equals(username)) {
				return true;
			}
		}
		return false;
	}

	public void removeUnresponsiveClients() {
		List<ClientConnectionAdapter> clientsToRemove = new ArrayList<ClientConnectionAdapter>();
		for (ClientConnectionAdapter clientConnection : getClients()) {
			if (clientConnection.clientDisconected()) {
				clientsToRemove.add(clientConnection);
			}
		}
		clients.removeAll(clientsToRemove);
	}

	public void addClient(ClientConnectionAdapter client) {
		clients.add(client);
		logger.info("Client " + client.getUsername() + " entered room: "
				+ this.roomName);
	}

	public void removeClient(ClientConnectionAdapter client) {
		clients.remove(client);
	}

	public String getChatTopic() {
		return chatTopic;
	}

	public void setChatTopic(String chatTopic) {
		this.chatTopic = chatTopic;
	}

	public String getRoomName() {
		return roomName;
	}
	
	public List<ClientConnectionAdapter> getClients() {
		return clients;
	}
	
}
