package xnet.core.http;

public class ServletFactory {
	public static ServletFilter filter = null;

	public static Servlet get(Request url) {
		if (filter == null) {
			return null;
		}
		return filter.getServlet(url);
	}
}
