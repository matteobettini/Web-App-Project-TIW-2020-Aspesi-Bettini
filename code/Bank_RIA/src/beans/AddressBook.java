package beans;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AddressBook {

	private int owner_id;
	private Map<Integer, Set<Integer>> contacts = new HashMap<>();
	
	public int getOwner_id() {
		return owner_id;
	}
	public void setOwner_id(int owner_id) {
		this.owner_id = owner_id;
	}
	public Map<Integer, Set<Integer>> getContacts() {
		return new HashMap<>(contacts);
	}
	
	public void putContact(int owner_id, int account_id) {
		if(contacts.containsKey(owner_id)) {
			contacts.get(owner_id).add(account_id);
		}else {
			Set<Integer> set = new HashSet<>();
			set.add(account_id);
			contacts.put(owner_id, set);
		}

	}
	

	
	
	
}
