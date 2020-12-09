package controllers;

import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import beans.BankAccount;
import beans.User;
import dao.AddressBookDAO;
import dao.BankAccountDAO;
import utils.ConnectionHandler;

/**
 * Servlet implementation class ToRegisterPage
 */
@WebServlet("/AddContact")
@MultipartConfig
public class AddContact extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddContact() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
		this.connection = ConnectionHandler.getConnection(servletContext);
    }
    
    @Override
    public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		String contactIdString = request.getParameter("contactId");
		String contactAccountIdString = request.getParameter("contactAccountId");
		
		if(contactAccountIdString == null || contactIdString == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("Missing parameter");
			return;
		}
		
		int contactId;
		int contactAccountId;
		
		try {
			contactAccountId = Integer.parseInt(contactAccountIdString);
			contactId = Integer.parseInt(contactIdString);
			
		}catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("Parameters are not integers");
			return;
		}
		
		HttpSession session = request.getSession(false);
		User currentUser = (User)session.getAttribute("currentUser");
		
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		BankAccount bankAccount;
		try {
			bankAccount = bankAccountDAO.getAccountById(contactAccountId);
		}catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;		
		}

		if(bankAccount == null || bankAccount.getUserId() != contactId) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);		
			response.getWriter().println("Chosen parameters are inconsistent with the database info, retry");
			return;
		}
		
		AddressBookDAO addressBookDAO = new AddressBookDAO(connection);
		boolean alreadyPresent = false;
		try {
			alreadyPresent = addressBookDAO.existsContactEntry(currentUser.getId(), contactAccountId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;		
		}
		
		if(alreadyPresent) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);		
			response.getWriter().println("Contact entry already exists");
			return;
		}
		
		try {
			addressBookDAO.addContactToAddressBook(currentUser.getId(), contactAccountId);
		}catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);	
		
	
	}

}
