package io.pivotal.pa.simplejms;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "consumerServlet", urlPatterns = { "/consume-message" })
public class ConsumerServlet extends HttpServlet {

	private static final long serialVersionUID = 2638127270022284917L;

	@Inject
	Consumer consumer;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String retVal = "Consumer";
		
		if(consumer == null) {
			retVal = "Consumer was null.";
			consumer = new Consumer();
		}

		retVal += consumer.doConsume();
		
		PrintWriter out = response.getWriter();
		out.println("Consumed message: " + retVal);
		out.close();
	}

}