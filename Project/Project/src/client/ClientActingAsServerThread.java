package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Properties;


public class ClientActingAsServerThread extends Thread{
	Socket objSocket;
	public ClientActingAsServerThread(Socket objSocket){
		this.objSocket = objSocket;
	}
	
	public void run(){
		try {
			ObjectOutputStream objClientServerOutStream = new ObjectOutputStream(objSocket.getOutputStream());
			ObjectInputStream objClientServerInStream = new ObjectInputStream(objSocket.getInputStream());
			String filename = (String) objClientServerInStream.readObject();
			Properties objProperty = new Properties();
			objProperty.load(new FileInputStream("napster.properties"));
			//while(true){
				
			File myfile = new File(objProperty.getProperty("fileLocation")+"\\"+filename);
			long length = myfile.length();
			byte [] mybytearray  = new byte [(int) length];
			objClientServerOutStream.writeObject((int)myfile.length());
			objClientServerOutStream.flush();
			FileInputStream fileInSt =new FileInputStream(myfile);
			BufferedInputStream objBufInStream =new BufferedInputStream(fileInSt);
			objBufInStream.read(mybytearray,0, (int)myfile.length());
			
			System.out.println("Sending " + "File" + "(" + mybytearray.length + " bytes)");
			objClientServerOutStream.write(mybytearray,0,mybytearray.length);
			objClientServerOutStream.flush();
			//}
		}catch(Exception e){
		}
	}

}
