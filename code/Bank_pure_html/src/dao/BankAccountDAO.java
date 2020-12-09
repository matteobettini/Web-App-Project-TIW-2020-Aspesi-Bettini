package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import beans.BankAccount;

public class BankAccountDAO {

	private Connection connection;

	public BankAccountDAO(Connection connection) {
		this.connection = connection;
	}
	
	public BankAccount getAccountById(int id) throws SQLException{

		BankAccount account = null;
		String performedAction = " finding an account by id";
		String query = "SELECT * FROM bank.bank_account WHERE id = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, id);
			resultSet = preparedStatement.executeQuery();
			
			while(resultSet.next()) {
				account = new BankAccount();
				account.setId(resultSet.getInt("id"));
				account.setUserId(resultSet.getInt("userid"));
				account.setName(resultSet.getString("name"));
				account.setBalance(resultSet.getBigDecimal("balance"));
			}
			
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
		return account;
	}
	
	public List<BankAccount> getAccountsByUserId(int userid) throws SQLException{

		List<BankAccount> accounts = new ArrayList<>();
		String performedAction = " finding accounts by user id";
		String query = "SELECT * FROM bank.bank_account WHERE userid = ? ORDER BY id ASC";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, userid);
			resultSet = preparedStatement.executeQuery();
			
			while(resultSet.next()) {
				BankAccount account = new BankAccount();
				account.setId(resultSet.getInt("id"));
				account.setUserId(resultSet.getInt("userid"));
				account.setName(resultSet.getString("name"));
				account.setBalance(resultSet.getBigDecimal("balance"));
				accounts.add(account);
			}
			
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
		return accounts;
	}
	
	public BankAccount getAccountByUserIdAndName(int userid, String name) throws SQLException{

		BankAccount account = null;
		String performedAction = " finding accounts by user id and account name";
		String query = "SELECT * FROM bank.bank_account WHERE userid = ? AND name = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, userid);
			preparedStatement.setString(2, name);
			resultSet = preparedStatement.executeQuery();
			
			while(resultSet.next()) {
				account = new BankAccount();
				account.setId(resultSet.getInt("id"));
				account.setUserId(resultSet.getInt("userid"));
				account.setName(resultSet.getString("name"));
				account.setBalance(resultSet.getBigDecimal("balance"));
			}
			
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
		return account;
	}
	
	public void createAccount(int userid, String name) throws SQLException {
		
		String performedAction = " creating a new bank account in the database";
		String queryAddUser = "INSERT INTO bank.bank_account (userid,name) VALUES(?,?)";
		PreparedStatement preparedStatementAddUser = null;	
		
		try {
			
			preparedStatementAddUser = connection.prepareStatement(queryAddUser);
			preparedStatementAddUser.setInt(1, userid);
			preparedStatementAddUser.setString(2, name);
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
