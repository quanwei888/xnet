package xnet.demo.httpserver;

import xnet.http.Request;
import xnet.http.Response;
import xnet.http.Servlet;

public class EchoServlet implements Servlet {

	public void doRequest(Request request, Response response) throws Exception {		 
		response.write("hello 中国");
	}

}
