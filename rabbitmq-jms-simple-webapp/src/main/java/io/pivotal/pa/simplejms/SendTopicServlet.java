package io.pivotal.pa.simplejms;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "senderServlet", urlPatterns = { "/send-topic-message" })
public class SendTopicServlet extends HttpServlet {

	private static final long serialVersionUID = 2638127270022516617L;
	
	@Inject
	private Sender sender;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String retVal = "Sender";
		
		if(sender == null) {
			retVal = "Sender was null.";
			sender = new Sender();
		}
		
		retVal += sender.doSend();

		PrintWriter out = response.getWriter();
		out.println("Sending message: "+retVal);
		out.close();
	}

}