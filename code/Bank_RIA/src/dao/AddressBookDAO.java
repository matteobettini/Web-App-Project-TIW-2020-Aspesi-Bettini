package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import beans.AddressBook;


public class AddressBookDAO {

	private Connection connection;
	
	public AddressBookDAO(Connection connection) {
		this.connection = connection;
	}
	
	public AddressBook getAddressBookById(int ownerId) throws SQLException{

		AddressBook addressBook = new AddressBook();
		addressBook.setOwner_id(ownerId);

		String performedAction = " constructing an addressbook by owner id";
		String query = "SELECT a.contact_account, b.userid FROM bank.address_book AS a JOIN bank.bank_account AS b ON a.contact_account = b.id WHERE a.owner_id = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, ownerId);
			resultSet = preparedStatement.executeQuery();
			
			while(resultSet.next()) 
				addressBook.putContact(resultSet.getInt("userid"), resultSet.getInt("contact_account"));
			
			
		}catch(SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction);
		}finally {
			try {
				resultSet.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the result set when" + performedAction);
			}
			try {
				preparedStatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement when" + performedAction);
			}
		}
		return addressBook;
	}
	
	public boolean existsContactEntry(int ownerId, int contact_account) throws SQLException{

		boolean result = false;

		String performedAction = " determining if a contact already exists";
		String query = "SELECT * FROM bank.address_book WHERE owner_id = ? AND contact_account = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, ownerId);
			preparedStatement.setInt(2, contact_account);
			resultSet = preparedStatement.executeQuery();
			
			if(resultSet.next()) 
				result = true;
			
			
		}catch(SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction);
		}finally {
			try {
				resultSet.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the result set when" + performedAction);
			}
			try {
				preparedStatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement when" + performedAction);
			}
		}
		return result;
	}
	
	public void addContactToAddressBook(int userid, int destAccountId) throws SQLException {
		
		String performedAction = " adding a new entry in an address book in the database";
		String queryAddUser = "INSERT INTO bank.address_book (owner_id, contact_account) VALUES(?,?)";
		PreparedStatement preparedStatementAddUser = null;	
		
		try {
			
			preparedStatementAddUser = connection.prepareStatement(queryAddUser);
			preparedStatementAddUser.setInt(1, userid);
			preparedStatementAddUser.setInt(2, destAccountId);
			preparedStatementAddUser.executeUpdate();
			
		}catch(SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction);
		}finally {
			try {
				preparedStatementAddUser.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement when" + performedAction);
			}
		}
	}
}
