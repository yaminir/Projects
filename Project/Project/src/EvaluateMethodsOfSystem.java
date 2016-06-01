import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import client.ClientOfNapster;


public class EvaluateMethodsOfSystem {

	
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
		
		Socket objClientSocket = new Socket("192.168.1.5",2132);
		ObjectOutputStream objOutStream = new ObjectOutputStream(objClientSocket.getOutputStream());
		ObjectInputStream objInStream = new ObjectInputStream(objClientSocket.getInputStream());
		Properties objProperty = new Properties();
		objProperty.load(new FileInputStream("napster.properties"));
		List<String> fileList = filesAtFolder(objProperty
				.getProperty("fileLocation"));
		
		ClientOfNapster objClientOfNapster = new ClientOfNapster();
		
		//uncomment the method want to test
		
		testRegister(objClientOfNapster,objOutStream, objInStream, fileList, objProperty);
			
		//testSearch( objClientOfNapster, objOutStream, objInStream, "build.xml");
		

	}
	
	//method to test registerAtServer
	public static void testRegister(ClientOfNapster objClientOfNapster,ObjectOutputStream objOutStream,ObjectInputStream objInStream,List<String> fileList,Properties objProperty) throws ClassNotFoundException, IOException{
		long starttime = System.currentTimeMillis();
		for(int i=1;i<1000;i++)
		{
		objClientOfNapster.registerAtServer(objOutStream, objInStream, fileList, objProperty);
		objOutStream.writeObject("2");
		objOutStream.flush();
		}
		System.out.println("Total Time in milliseconds:"+(starttime -System.currentTimeMillis()));
	}
	
	//method to test testSearch
	public static void testSearch(ClientOfNapster objClientOfNapster,ObjectOutputStream objOutStream,ObjectInputStream objInStream,String filename) throws ClassNotFoundException, IOException{
		long starttime = System.currentTimeMillis();
		for(int i=1;i<250;i++)
		{
		objClientOfNapster.searchPeersWithFile( objOutStream,objInStream, filename);
		objOutStream.writeObject("1");
		objOutStream.flush();
		objOutStream.writeObject("2");
		objOutStream.flush();
		}
		System.out.println("Total Time in milliseconds:"+(starttime -System.currentTimeMillis()));
	}
	
	public static List<String> filesAtFolder(String path){
		List<String> fileList = new ArrayList<>();
		File objFile = new File(path);
		for(File listOfFiles : (objFile.listFiles())){
			if(listOfFiles.isFile()){
				fileList.add(listOfFiles.getName());
			}
		}
		return fileList;
	}

}
