
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class NapsterServerSideThread extends Thread{
	Socket objSocket;
	Map<String,List<String>> indexingMap;
	
	NapsterServerSideThread(){
	}
	
	NapsterServerSideThread(Socket objSocket , Map<String,List<String>> indexingMap){
		this.objSocket = objSocket;
		this.indexingMap = indexingMap;
	}
	public void run(){
		try {
		ObjectOutputStream objOutStream = new ObjectOutputStream(objSocket.getOutputStream());
		ObjectInputStream objInStream = new ObjectInputStream(objSocket.getInputStream());
		while(true){
			
			//Server receiving client's file details
			List<String> fileLst =  (List<String>) objInStream.readObject();
			
			//Server receiving client's port on which it is acting as a Server.
			String clientsPortAsServer = (String) objInStream.readObject();
			//System.out.println("Index server before Client "+messege+" Connects :"+indexingMap);
			updateIndexServer(indexingMap,objSocket,fileLst,clientsPortAsServer);
			System.out.println("Index server afterClient  Connects :"+indexingMap);
			
			//Sending Registered Acknowledgment.
			objOutStream.writeObject("Hello Client : "+objSocket.getInetAddress().getHostName()+" - You are registered Successfully");
			objOutStream.flush();
			
			//Reading clients input to perform action.
			String input = (String) objInStream.readObject();
			switch(input){
			case "1":
				while(true){
				lookUpForClientInPut( objInStream, objOutStream);
				String choice = (String) objInStream.readObject();
				if(choice=="2")
				{
					lookUpForClientInPut( objInStream, objOutStream);
				}
				}
			case "2" :
				break;
			default:
				while(true){}
			}
		}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	 /* Method : updateIndexServer
	 * ------------------------------------------------------------------------------------------------------
	 * @param : Map<String,List<String>>
	 * @param : Socket
	 * @param : List<String>
	 * @param : String
	 * 
	 * return : void
	 *
	 * To update index server's map.
	 */
	public void updateIndexServer(Map<String,List<String>> indexMap,Socket objSocket,List<String> fileLst,String Port) throws UnknownHostException{
		for(String objFile: fileLst){
			if(indexMap.containsKey(objFile)){
				List<String> lstOfStr =indexMap.get(objFile);
				if(!lstOfStr.contains(objSocket.getInetAddress().getHostAddress()+"-"+Port))
				lstOfStr.add(objSocket.getInetAddress().getHostAddress()+"-"+Port);
				indexMap.put(objFile, lstOfStr);
			}else{
				List<String> lstOfStr = new ArrayList<String>();
				lstOfStr.add(objSocket.getInetAddress().getHostAddress()+"-"+Port);
				indexMap.put(objFile, lstOfStr);
			}
		}
	}
	
	 /* Method : lookupIndex
		 * ------------------------------------------------------------------------------------------------------
		 * @param : Map<String,List<String>>
		 * @param : filename
		 * 
		 * return : List<String>
		 *
		 * To search file location.
		 */
	public List<String> lookupIndex(Map<String,List<String>> indexMap,String filename){
		if(indexMap.containsKey(filename)){
			return indexMap.get(filename);
		}else{
			List<String> outPut = new ArrayList<String>();
			outPut.add("File Not Found with any Peer");
			return outPut;
		}
	}
	
	public void lookUpForClientInPut(ObjectInputStream objInStream,ObjectOutputStream objOutStream) throws ClassNotFoundException, IOException{
		System.out.println("--------------START-------------");
		try{
		String msgg = (String) objInStream.readObject();
		List<String> lstout = lookupIndex(indexingMap,msgg);
		objOutStream.writeObject(lstout);
		objOutStream.flush();
		System.out.println("--------------END-------------");
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	

}
