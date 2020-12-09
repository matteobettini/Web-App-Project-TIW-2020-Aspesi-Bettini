package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import beans.BankAccount;
import beans.Transfer;
import beans.User;
import dao.BankAccountDAO;
import dao.TransferDAO;
import utils.ConnectionHandler;
import utils.PathUtils;
import utils.TemplateHandler;

/**
 * Servlet implementation class ToRegisterPage
 */
@WebServlet("/GoToAccountStatus")
public class GoToAccountStatus extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToAccountStatus() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
		this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		String accountIdString = request.getParameter("accountId");
		
		if(accountIdString == null) {
			forwardToErrorPage(request, response, "Null account id, when accessing account details");
		}
		
		int accountId;
		try {
			accountId = Integer.parseInt(accountIdString);
		}catch (NumberFormatException e) {
			forwardToErrorPage(request, response, "Chosen account id is not a number, when accessing account details");
			return;
		}
		
		HttpSession session = request.getSession(false);
		User currentUser = (User)session.getAttribute("currentUser");
		
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		BankAccount bankAccount;
		try {
			bankAccount = bankAccountDAO.getAccountById(accountId);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;		
		}
		
		if(bankAccount == null || bankAccount.getUserId() != currentUser.getId()) {
			forwardToErrorPage(request, response, "Account not existing or not yours");
			return;
		}
		
		List<Transfer> transfers;
		TransferDAO transferDAO = new TransferDAO(connection);
		try {
			transfers = transferDAO.getTransfersByAccountId(accountId);
		}catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;	
		}
		
		request.setAttribute("account", bankAccount);
		request.setAttribute("transfers", transfers);
		forward(request, response, PathUtils.pathToAccountStatusPage);
		
	}
	
	private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response, String error) throws ServletException, IOException{
		
		request.setAttribute("error", error);
		forward(request, response, PathUtils.pathToErrorPage);
		return;
	}
	
	private void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException{
		
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
