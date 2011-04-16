package xnet.demo.httpserver;

import xnet.core.http.Request;
import xnet.core.http.Response;
import xnet.core.http.Servlet;

public class TimeServlet implements Servlet {

	public void doRequest(Request request, Response response) throws Exception {		 
		Long now = System.currentTimeMillis();
		response.write(now.toString());
	}

}
