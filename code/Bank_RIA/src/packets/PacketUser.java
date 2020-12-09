package packets;

public class PacketUser {

	private String name;
	private int id;
	
	public PacketUser(String name, int id) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}
	
	
}
