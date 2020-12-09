package controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

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
@WebServlet("/MakeTransfer")
public class MakeTransfer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;
	private int sourceAccountId;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MakeTransfer() {
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
		doPost(request, response);
	}
	
	private void forwardToTransferFailedPage(HttpServletRequest request, HttpServletResponse response, String reasonOfFailure) throws ServletException, IOException{
		
		request.setAttribute("reason", reasonOfFailure);
		request.setAttribute("accountId", sourceAccountId);
		forward(request, response, PathUtils.pathToTransferFailedPage);
		return;
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
		
		String destUserIdString = request.getParameter("destUserId");
		String destAccountIdString = request.getParameter("destAccountId");
		String sourceAccountIdString = request.getParameter("sourceAccountId");
		String amountString = request.getParameter("amount");
		String reason = request.getParameter("reason");
		
		if(destAccountIdString == null || destUserIdString == null || sourceAccountIdString == null || amountString == null || reason == null) {
			forwardToErrorPage(request, response, "Some data requested is null, when making a transfer");
			return;
		}
		
		int destAccountId;
		int destUserId;
		BigDecimal amount;
		
		try {
			sourceAccountId = Integer.parseInt(sourceAccountIdString);
			destAccountId = Integer.parseInt(destAccountIdString);
			destUserId = Integer.parseInt(destUserIdString);
			amount = new BigDecimal(amountString.replace(",","."));
		}catch (NumberFormatException e) {
			forwardToErrorPage(request, response, "Some values requested are not numbers, when making a transfer");
			return;
		}
		
	
		HttpSession session = request.getSession(false);
		User currentUser = (User)session.getAttribute("currentUser");
			
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		BankAccount sourceAccount;
		BankAccount destAccount;
	
		try {
			sourceAccount = bankAccountDAO.getAccountById(sourceAccountId);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		try {
			destAccount = bankAccountDAO.getAccountById(destAccountId);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		
		if(sourceAccount == null) {
			forwardToTransferFailedPage(request, response, "Source account does not exist");
			return;
		}
		if(destAccount == null) {
			forwardToTransferFailedPage(request, response, "Destination account does not exist");
			return;
		}
		if(sourceAccount.getUserId() != currentUser.getId()) {
			forwardToTransferFailedPage(request, response, "Source account does not belong to the current user");
			return;
		}
		if(destAccount.getUserId() != destUserId) {
			forwardToTransferFailedPage(request, response, "Destination account does not belong to the selected destination user");
			return;
		}
		if(sourceAccountId == destAccountId) {
			forwardToTransferFailedPage(request, response, "Cannot make transfers on the same account");
			return;
		}
		if(amount.compareTo(new BigDecimal(0)) != 1) {
			forwardToTransferFailedPage(request, response, "Transfer amount must be greater than 0");
			return;
		}
		if(sourceAccount.getBalance().subtract(amount).compareTo(new BigDecimal(0)) == -1) {
			forwardToTransferFailedPage(request, response, "You don't have enough money on this account to make this transfer");
			return;
		}
		
		TransferDAO transferDAO = new TransferDAO(connection);
		try {
			transferDAO.makeTransfer(sourceAccountId, destAccountId, reason, amount);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;		
		}
		
		try {
			sourceAccount = bankAccountDAO.getAccountById(sourceAccountId);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		try {
			destAccount = bankAccountDAO.getAccountById(destAccountId);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		
		Transfer transfer = new Transfer();
		transfer.setSourceAccountID(sourceAccountId);
		transfer.setDestinationAccountID(destAccountId);
		transfer.setAmount(amount);
		transfer.setReason(reason);
		
		session.setAttribute("transfer", transfer);
		session.setAttribute("sourceAccount", sourceAccount);
		session.setAttribute("destAccount", destAccount);
		session.setAttribute("transferInfoShown", false);
		
		response.sendRedirect(getServletContext().getContextPath() + PathUtils.goToTransferConfirmedServletPath);
	
	}

}
