package xnet.test;

import java.nio.ByteBuffer;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

public class Test {
	static Logger logger = Logger.getLogger(Test.class);

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		// Set up a simple configuration that logs on the console.
		BasicConfigurator.configure();

		logger.info("Entering application."); 
		logger.info("Exiting application.");

	}

}
