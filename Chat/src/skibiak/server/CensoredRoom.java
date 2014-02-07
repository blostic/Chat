package skibiak.server;

import java.io.IOException;

public class CensoredRoom extends Room {

	public CensoredRoom(Server server, String roomName, String chatTopic,String roomMaster) {
		super(server, roomName, chatTopic, roomMaster);
		this.startRoom();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void annouceMessage(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void readMessages() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeUnresponsiveClients() {
		// TODO Auto-generated method stub

	}

}
