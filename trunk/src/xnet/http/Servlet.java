package xnet.http;

public interface Servlet {
	public void doRequest(Request request, Response response) throws Exception;
}
