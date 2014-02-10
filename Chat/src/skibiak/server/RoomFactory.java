package skibiak.server;

public class RoomFactory {
	public static Room getInstance(Server server, String name, String type,
			String topic) throws ClassNotFoundException {
		switch (type.toLowerCase()) {
		case "public":
			return new PublicRoom(server, name, topic);
		case "censored":
			return new CensoredRoom(server, name, topic);
		default:
			throw new ClassNotFoundException();
		}
	}
}
