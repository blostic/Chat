package skibiak.server;

public class RoomFactory {
	public static Room getInstance(Server server, String name, String type,
			String topic, String master) throws ClassNotFoundException {
		switch (type.toLowerCase()) {
		case "public":
			return new PublicRoom(server, name, topic, master);
		case "censored":
			return new CensoredRoom(server, name, topic, master);
		default:
			throw new ClassNotFoundException();
		}
	}
}
