package xnet.demo.httpserver;

import xnet.core.http.Request;
import xnet.core.http.Response;
import xnet.core.http.Servlet;

public class ErrorServlet implements Servlet {

	public void doRequest(Request request, Response response) throws Exception {
		response.write("this action not found");
	}

}