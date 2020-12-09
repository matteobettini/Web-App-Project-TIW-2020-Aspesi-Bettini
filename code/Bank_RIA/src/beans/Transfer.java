package beans;

import java.math.BigDecimal;
import java.util.Date;

public class Transfer {

	private int sourceAccountID;
	private int destinationAccountID;
	private Date timestamp;
	private String reason;
	private BigDecimal amount;
	
	
	public int getSourceAccountID() {
		return sourceAccountID;
	}
	public void setSourceAccountID(int sourceAccountID) {
		this.sourceAccountID = sourceAccountID;
	}
	public int getDestinationAccountID() {
		return destinationAccountID;
	}
	public void setDestinationAccountID(int destinationAccountID) {
		this.destinationAccountID = destinationAccountID;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	

}
