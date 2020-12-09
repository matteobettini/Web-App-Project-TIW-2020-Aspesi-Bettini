package controllers;

import java.io.IOException;
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
import beans.User;
import dao.BankAccountDAO;
import utils.ConnectionHandler;
import utils.PathUtils;
import utils.TemplateHandler;

/**
 * Servlet implementation class ToRegisterPage
 */
@WebServlet("/CreateAccount")
public class CreateAccount extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateAccount() {
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
		
		
		String accountName = request.getParameter("accountName");
		
		if(accountName == null) {
			forwardToErrorPage(request, response, "Null account name");
			return;
		}
		
		HttpSession session = request.getSession(false);
		User currentUser = (User)session.getAttribute("currentUser");
		
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		BankAccount bankAccount;
		try {
			bankAccount = bankAccountDAO.getAccountByUserIdAndName(currentUser.getId(), accountName);
		}catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;	
		}
		
		if(bankAccount != null) {
			session.setAttribute("createAccountExists", true);
			response.sendRedirect(getServletContext().getContextPath() + PathUtils.goToHomeServletPath);
			return;
		}
			
		
		try {
			bankAccountDAO.createAccount(currentUser.getId(), accountName);
		}catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;		
		}
		
		response.sendRedirect(getServletContext().getContextPath() + PathUtils.goToHomeServletPath);
	
	}

}
