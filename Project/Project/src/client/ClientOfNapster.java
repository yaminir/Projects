package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ClientOfNapster {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		ClientOfNapster objClientOfNapster = new ClientOfNapster();
		Properties objProperty = new Properties();
		objProperty.load(new FileInputStream("napster.properties"));
		try {
			
			ClientActingAsServer objClientActingAsServer = new ClientActingAsServer();
			objClientActingAsServer.start();		
			
			System.out.println(objProperty.getProperty("IndexServerIP"));
			Socket objClientSocket = new Socket(objProperty.getProperty("IndexServerIP"),Integer.parseInt(objProperty.getProperty("indexingServerPort")));
			ObjectOutputStream objOutStream = new ObjectOutputStream(objClientSocket.getOutputStream());
			ObjectInputStream objInStream = new ObjectInputStream(objClientSocket.getInputStream());

			while(true){
				// Client sending file details he has
				List<String> fileList = filesAtFolder(objProperty
						.getProperty("fileLocation"));

				// Registering client
				objClientOfNapster.registerAtServer(objOutStream,objInStream,fileList,objProperty);
		
				//menu for the User Input
				objClientOfNapster.menuForUser( objClientOfNapster,objInStream , objOutStream , objProperty);
				}
			   
				
		} catch (IOException | ClassNotFoundException  e) { 
		}
	}
	
	 /* Method : filesAtFolder
	 * ------------------------------------------------------------------------------------------------------
	 * @param : String path
	 * return : List<String>
	 *
	 * To get the files present at a location.
	 */
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
	
	public void registerAtServer(ObjectOutputStream objOutStream,ObjectInputStream objInStream,List<String> fileList,Properties objProperty) throws IOException, ClassNotFoundException{
		objOutStream.writeObject(fileList);
		objOutStream.flush();
		
		//sending clients port number on which its acting as server
		String clientsPortAsServer =objProperty.getProperty("nodeServerPort");
		objOutStream.writeObject(clientsPortAsServer);
		objOutStream.flush();
		
		//Acknowledgment from server & printing it
		System.out.println("Message from Server :"+objInStream.readObject());
	}
	
	 public void downloadFileFromPeer(String peerWithFileIP,String peerWithFilePort,String filename,Properties objProperty) throws NumberFormatException, UnknownHostException, IOException{
			Socket objpeerSocket = new Socket(peerWithFileIP, Integer.parseInt(peerWithFilePort));
			try {
				ObjectOutputStream objClientOutStream = new ObjectOutputStream(objpeerSocket.getOutputStream());
		    	ObjectInputStream objClientInputStream =  new ObjectInputStream(objpeerSocket.getInputStream());
		
				objClientOutStream.writeObject(filename);
				objClientOutStream.flush();
				long fileSize = (Integer)objClientInputStream.readObject();
		
		    	byte [] mybytearray  = new byte [ (int) fileSize];
	    		OutputStream  fileOPstream = new FileOutputStream(objProperty.getProperty("fileLocation")+filename);
		
	    		BufferedOutputStream buffOPS = new BufferedOutputStream(fileOPstream);
				objClientInputStream.readFully(mybytearray);
				buffOPS.write(mybytearray, 0,(int) fileSize);
				buffOPS.flush();
				fileOPstream.flush();
				System.out.println("**********File Downloaded Successfully***********");
			} catch (Exception e) 
				{
				e.printStackTrace();
				}
			
	 }
	 
	 public void menuForUser(ClientOfNapster objClientOfNapster,ObjectInputStream objInStream ,ObjectOutputStream objOutStream ,Properties objProperty) throws IOException, ClassNotFoundException{
		//MENU For Client :::::
			System.out.println();
			System.out.println("MENU :::::::::::::::");
			System.out.println("Select one of the Operations :");
			System.out.println("1 - To search for a File.");
			System.out.println("2 - To Exit(Disconnect from Server).");
			System.out.println("Note : Any other Key press will Keep client connected.");
			System.out.println("Enter your choice : ");
			
			//Taking Input from client.
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String input = reader.readLine();
			
			
			//sending clients input to the Server to perform action.
			objOutStream.writeObject(input);
			objOutStream.flush();
			
			switch(input){
			case "1" :
				fileSearchMenu( objClientOfNapster, objInStream , objOutStream , objProperty, reader);
				break;
			case "2" :
		    	System.out.println("You will be disconnected");
		    	System.exit(0);
			default :
				System.out.println("You will be remain connected");
				while(true){}
			}
	 }
	 
	 public void fileSearchMenu(ClientOfNapster objClientOfNapster,ObjectInputStream objInStream ,ObjectOutputStream objOutStream ,Properties objProperty,BufferedReader reader) throws IOException, ClassNotFoundException{
		 	System.out.println("");	
		 	System.out.println("Enter File Name : ");
		    String filename =reader.readLine();
		    
		    List<String> ipOutPut = objClientOfNapster.searchPeersWithFile(objOutStream,objInStream,filename);
		    
		    if(ipOutPut.get(0).equalsIgnoreCase("File Not Found with any Peer"))
		    {
		    System.out.println("SEARCH OUTPUT :::::: File Not Found with any Peer");
		    }else
		    {
		    	System.out.println();
		    	System.out.println("SEARCH OUTPUT :::::: ");
		    	for(String peerDetails : ipOutPut){
		    		String[] peer = peerDetails.split("-");
		    		String peerIP = peer[0];
		    		String peerPort = peer[1]; 
		    		System.out.println("location of files ::::::: IP of Peer -"+peerIP+", Port number -"+peerPort);
		    }
		    
		    System.out.println("");
		    System.out.println("MENU:::Select Options to perform Operations");
		    System.out.println("1 - To Connect to peer and download file");
		    System.out.println("2 - To Search another file");
		    System.out.println("3 - Exit");
		    String[] peer = ipOutPut.get(0).split("-");
		    String peerWithFileIP = peer[0];
			String peerWithFilePort = peer[1];
			System.out.println();
			System.out.println("Enter your choice : ");
			String actionToPerform = reader.readLine();
			
			objOutStream.writeObject(actionToPerform);
			objOutStream.flush();
			
			switch(actionToPerform){
			case "1":
				objClientOfNapster.downloadFileFromPeer( peerWithFileIP, peerWithFilePort, filename, objProperty);
				break;
			case "2" :
				fileSearchMenu( objClientOfNapster, objInStream , objOutStream , objProperty, reader);
				break;
			case "3" :
				System.out.println("You will be disconnected");
		    	System.exit(0);
				break;
			default :
				System.out.println("Wrong InPut");
			}
		}
	 }
	 
	 @SuppressWarnings("unchecked")
	public List<String> searchPeersWithFile(ObjectOutputStream objOutStream,ObjectInputStream objInStream,String filename) throws IOException, ClassNotFoundException{
		 System.out.println("---------------Test----------------");	
		 objOutStream.writeObject(filename);
		    objOutStream.flush();
		    List<String> ipOutPut = (List<String>) objInStream.readObject();
		    return ipOutPut;
	}
}
