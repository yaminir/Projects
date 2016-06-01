package client;

import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;


public class ClientActingAsServer extends Thread{
	
	public static  int ports;

	public void run(){
		try {
			Properties objProperty = new Properties();
			objProperty.load(new FileInputStream("napster.properties"));
			ports = Integer.parseInt(objProperty.getProperty("nodeServerPort"));
			
			ServerSocket objClientSerSocket = new ServerSocket(ports);
			System.out.println("Client(as a Server) Waiting for Connection at Port :"+ports);
			while (true) {
				Socket objClientServerSocket = objClientSerSocket.accept();
				new ClientActingAsServerThread(objClientServerSocket).start();
			}
			
		}catch(Exception e){}
	}
	
}
