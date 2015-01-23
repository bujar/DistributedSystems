import java.util.ArrayList;


public class MessagePasser {
    //done
	
	public static ArrayList<Host> hostList = new ArrayList<Host>();
	public static int seqNum;
	
	public MessagePasser(String pathName, String localName){
		seqNum = 0;
		// for host in configFile
		//new Socket host
		Host h = new Host(null, null, null);
		hostList.add(h);
	}
	
}

