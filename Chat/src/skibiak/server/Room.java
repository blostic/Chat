package skibiak.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public abstract class Room implements Runnable {
	private static Logger logger = Logger.getLogger(Room.class);
	private String chatTopic;
	private String roomName;
	private String roomMaster;

	final List<ClientConnectionAdapter> clients;
	protected final Server server;
	
	public Room(Server server, String roomName, String chatTopic, String roomMaster) {
		this.chatTopic = chatTopic;
		this.clients = new CopyOnWriteArrayList<ClientConnectionAdapter>();
		this.server = server;
		this.setRoomMaster(roomMaster);
		this.setRoomName(roomName);
		
		PropertyConfigurator.configure("log4j.properties");
		logger.setLevel(Level.INFO);
	}

	public List<ClientConnectionAdapter> getClients() {
		return clients;
	}

	public void startRoom() {
		new Thread(this).start();
		
		new Runnable() {

			@Override
			public void run() {
				while (server.isActive()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					removeUnresponsiveClients();
				}
			}
		};

	}

	public void removeUnresponsiveClients() {
		List<ClientConnectionAdapter> clientsToRemove = new ArrayList<ClientConnectionAdapter>();
		for (ClientConnectionAdapter clientConnection : getClients() ) {
			if (clientConnection.clientDisconected()) {
				clientsToRemove.add(clientConnection);
			}
		}
		for (ClientConnectionAdapter  client: clientsToRemove){
			removeClient(client);
		}
	}
	
	public void addClient(ClientConnectionAdapter client) {
		logger.info("Client " + client.getUsername() + " entered room: "
				+ this.roomName);
		this.clients.add(client);
	}

	public void removeClient(ClientConnectionAdapter client) {
		logger.info("Client " + client.getUsername() + " left room: "
				+ this.roomName);
		clients.remove(client);
	}

	public String getChatTopic() {
		return this.chatTopic;
	}

	public void setChatTopic(String chatTopic) {
		this.chatTopic = chatTopic;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	
	public abstract void annouceMessage(String message);

	public abstract void readMessages() throws IOException;

	public String getRoomMaster() {
		return roomMaster;
	}

	public boolean containUser(String username){
		for (ClientConnectionAdapter clientConnection : this.getClients()) {
			if (clientConnection.getUsername().equals(username)) {
				return true;
			}
		}
		return false;
	}
	
	public void setRoomMaster(String roomMaster) {
		this.roomMaster = roomMaster;
	}

}
