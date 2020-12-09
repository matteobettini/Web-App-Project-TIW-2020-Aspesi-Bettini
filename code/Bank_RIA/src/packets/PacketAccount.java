package packets;

import java.util.List;
import beans.BankAccount;
import beans.Transfer;

public class PacketAccount {

	private BankAccount account;
	private List<Transfer> transfers;
	
	public PacketAccount(BankAccount account, List<Transfer> transfers){
		this.account = account;
		this.transfers = transfers;
	}
	
	public BankAccount getAccount(){
		return this.account;
	}

	public List<Transfer> getTransfers(){
		return this.transfers;
	}
}
