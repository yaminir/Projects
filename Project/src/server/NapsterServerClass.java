/*
 *  NapsterServerClass.java
 *

 */
package server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class NapsterServerClass {

	//Server Port
	public static  int port;
	
	/* Map to Hold Data structures about Peers (Key - file name & value - List of Strings with information 
	 * like client IP & port.
	 */
	public static Map<String,List<String>> indexingMap = new HashMap<String, List<String>>();
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		Properties objProperty = new Properties();
		objProperty.load(new FileInputStream("napster.properties"));
		port = Integer.parseInt(objProperty.getProperty("indexingServerPort"));
		new NapsterServerClass().callRunServer();
	}
	
	/*
	 * Method : callRunServer
	 * ------------------------------------------------------------------------------------------------------
	 * @param : void
	 * return : void
	 *
	 * To create Server socket & accept client multi threaded request.
	 */
	public void callRunServer(){
		try {
			ServerSocket objServerSocket = new ServerSocket(port);
			System.out.println("Server Waiting for Connection at Port :"+port);
			while (true) {
				Socket objClientSocket = objServerSocket.accept();
				new NapsterServerSideThread(objClientSocket, indexingMap).start();
			}
		} catch (IOException e) {
		}
	}
}
