package controllers;

import java.io.IOException;

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
import utils.PathUtils;
import utils.TemplateHandler;

/**
 * Servlet implementation class ToRegisterPage
 */
@WebServlet("/GoToTransferConfirmedPage")
public class GoToTransferConfirmedPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToTransferConfirmedPage() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
		this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		HttpSession session = request.getSession(false);
		
		
		BankAccount sourceAccount = (BankAccount)session.getAttribute("sourceAccount");
		BankAccount destAccount = (BankAccount)session.getAttribute("destAccount");
		Transfer transfer = (Transfer)session.getAttribute("transfer");
		Boolean transferInfoShown = (Boolean)session.getAttribute("transferInfoShown");
		
		if(sourceAccount == null || destAccount == null || transfer == null || transferInfoShown == null) {
			forwardToErrorPage(request, response, "No transfer to show");
			return;
		}
		
		request.setAttribute("transferInfoShown", transferInfoShown);
		
		if(!transferInfoShown) 
			session.setAttribute("transferInfoShown", true);
			
	
		forward(request, response, PathUtils.pathToTransferConfirmedPage);
	}
	
	private void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException{
		
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
		
	}
	
	private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response, String error) throws ServletException, IOException{
		
		request.setAttribute("error", error);
		forward(request, response, PathUtils.pathToErrorPage);
		return;
	}


}
