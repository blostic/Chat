package skibiak.server;

public class PublicRoom extends Room {
	public PublicRoom(Server server, String roomName, String chatTopic) {
		super(server, roomName, chatTopic);
		this.startRoom();
	}

	@Override
	public void annouceMessage(String message) {
		System.out.println(" MESSAGE [" + getRoomName() + "] " + message);
		for (ClientConnectionAdapter connection : this.getClients()) {
			connection.sendMessage(message);
		}
	}

}
