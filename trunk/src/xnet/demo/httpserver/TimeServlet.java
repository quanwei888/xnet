package xnet.demo.httpserver;

import xnet.http.Request;
import xnet.http.Response;
import xnet.http.Servlet;

public class TimeServlet implements Servlet {

	public void doRequest(Request request, Response response) throws Exception {		 
		Long now = System.currentTimeMillis();
		response.write(now.toString());
	}

}
