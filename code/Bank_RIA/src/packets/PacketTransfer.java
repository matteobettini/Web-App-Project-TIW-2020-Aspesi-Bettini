package packets;

import beans.BankAccount;
import beans.Transfer;

public class PacketTransfer {

	private BankAccount sourceAccount;
	private BankAccount destAccount;
	private Transfer transfer;
	
	public PacketTransfer(BankAccount sourceAccount, BankAccount destAccount, Transfer transfer) {
		super();
		this.sourceAccount = sourceAccount;
		this.destAccount = destAccount;
		this.transfer = transfer;
	}

	public BankAccount getSourceAccount() {
		return sourceAccount;
	}


	public BankAccount getDestAccount() {
		return destAccount;
	}

	

	public Transfer getTransfer() {
		return transfer;
	}


	
	
	
	
}
