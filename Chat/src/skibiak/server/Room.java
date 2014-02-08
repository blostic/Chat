package skibiak.server;

import java.io.IOException;
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
	private ClientRequestHandler requestHandler;
	
	protected final List<ClientConnectionAdapter> clients;
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

	public void startRoom() {
		new Thread(this).start();
	}

	public void addClient(ClientConnectionAdapter client) {
		logger.info("Client " + client.getNickname() + " entered room: "
				+ this.roomName);
		this.clients.add(client);
	}

	public void removeClient(ClientConnectionAdapter client) {
		logger.info("Client " + client.getNickname() + " left room: "
				+ this.roomName);
		this.clients.remove(client);
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
	
	public ClientRequestHandler getRequestHandler(){
		return this.requestHandler;
	}
	
	public abstract void annouceMessage(String message);

	public abstract void readMessages() throws IOException;

	public String getRoomMaster() {
		return roomMaster;
	}

	public void setRoomMaster(String roomMaster) {
		this.roomMaster = roomMaster;
	}

}