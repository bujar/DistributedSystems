import java.awt.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;


public class MessagePasser {
    //done
	
	public static ArrayList<Host> hostList = new ArrayList<Host>();
	public static int seqNum;
	
	public MessagePasser(String pathName, String localName){
		seqNum = 0;
		
		Map<String, ArrayList<Map<String, Object>>> data = getYamlData(pathName);
		ArrayList<Map<String, Object>> config = data.get("configuration");

		for (Map<String, Object> key : config){
			
			String name = (String) key.get("name");
			String ipAddr = (String) key.get("ip");
			 int port = (Integer) key.get("port");
			Integer p = Integer.valueOf(port);
			Socket connection = newConnection(ipAddr, p);
			Host host = new Host(name, connection, ipAddr);
			hostList.add(host);
			
		}
		System.out.println(hostList);
	}
	
	public Map<String, ArrayList<Map<String, Object>>> getYamlData(String pathName){
		InputStream input = null;
		try {
			input = new FileInputStream(new File(
			        pathName));
		} catch (FileNotFoundException e) {
			System.out.println("Could not find configuration file");
			e.printStackTrace();
		}
		Yaml yaml = new Yaml();
		Map<String, ArrayList<Map<String, Object>>> configData = (Map<String, ArrayList<Map<String, Object>>>) yaml
				.load(input);
		return configData;
	}
	
	public Socket newConnection(String ipAddr, int port) {
		Socket connection = null;
		try {
			connection = new Socket(ipAddr, port);
		} catch (UnknownHostException e) {
			System.out.println("Could not establish connection to " + ipAddr);
		} catch (IOException e) {
			System.out.println("Could not establish connection to " + ipAddr);
		}
		return connection;
	}
	
}