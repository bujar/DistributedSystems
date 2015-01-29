
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.yaml.snakeyaml.Yaml;

public class MessagePasser {
    //done

    public ArrayList<Host> hostList = new ArrayList<Host>();
    public ArrayList<Rule> sendRules = new ArrayList<Rule>();
    public ArrayList<Rule> recvRules = new ArrayList<Rule>();
    public int seqNum;
    public static LinkedList<Message> recvDelayQueue;
    public LinkedList<Message> sendDelayQueue;
    public  static String configFile;
    public String localSource;
    public MessagePasser(String pathName, String localName) {
        seqNum = 0;
        configFile = pathName;
        localSource = localName;
        sendDelayQueue = new LinkedList<Message>();
        recvDelayQueue = new LinkedList<Message>();
        
        Map<String, ArrayList<Map<String, Object>>> data = getYamlData(pathName);
        ArrayList<Map<String, Object>> config = data.get("configuration");

        String action = "bind";
        Integer localport = 0;
        Host localhost = null;
        boolean myturn = false;
        int hostcounter = 1;

        int listencounter = 0;
        int totalnodes = 0;
        //iterate through config file to find own info
        for (Map<String, Object> key : config) {
            String name = (String) key.get("name");
            if (name.equals(localName)) {
                String ipAddr = (String) key.get("ip");
                int port = (Integer) key.get("port");
                localport = Integer.valueOf(port);
                localhost = new Host(name, null, ipAddr);
                listencounter = totalnodes;
            }
            totalnodes++;
        }
        
        //parse rules
		ArrayList<Map<String, Object>> sendRule = data.get("sendRules");
		for (Map<String, Object> key : sendRule) {
			String a = (String) key.get("action");
			String src = (String) key.get("src");
			String dst = (String) key.get("dest");
			String kind = (String) key.get("kind");
			int seq = -1;
			if (key.get("seqNum") != null)
				seq = (Integer) key.get("seqNum");
			Rule rule = new Rule(a, src, dst, kind, seq);
			sendRules.add(rule);
		}
		ArrayList<Map<String, Object>> recvRule = data.get("receiveRules");
		for (Map<String, Object> key : recvRule) {
			String a = (String) key.get("action");
			String src = (String) key.get("src");
			String dst = (String) key.get("dest");
			String kind = (String) key.get("kind");
			int seq = -1;
			if (key.get("seqNum") != null)
				seq = (Integer) key.get("seqNum");
			Rule rule = new Rule(a, src, dst, kind, seq);
			recvRules.add(rule);
		}
		
        //listen first
        if (listencounter > 0) {
            try {
                System.out.println(localName + " waiting for connection");
                ServerSocket server = (new ServerSocket(localport));

                while (hostList.size() < listencounter) {
                    Socket connection = server.accept();
                    ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                    System.out.println(localName + " got a new connection");
                    //  do {
                    Host received = (Host) input.readObject();
                    System.out.println(localName + " received object from " + received.name);

                    System.out.println(localName + " connecting to " + received.name + " over port " + received.port + " with address " + received.address);
                    Socket connection2 = new Socket(received.address, received.port);
                    if (connection2.isConnected()) {
                        System.out.println(localName + " final connection to " + received.name + " succeeded");
                    }
                    //connection2.getInputStream().read(); //should block until DONEPING is received
                    received.sock = new SocketHandler(connection2);
                    hostList.add(received);
                    connection.close();
                    System.out.println(localName + " added one host. " + hostList.size() + " hosts connected");
                    //  } while (input.available() > 0);
                }

            } catch (IOException ex) {
                System.out.println(localName + " error listening " + ex.toString());
                Logger.getLogger(MessagePasser.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                System.out.println(localName + " error listening " + ex.toString());
                Logger.getLogger(MessagePasser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /*try {
         Thread.sleep(5000);
         System.out.println(localName+"'s TURN!!!!!");
         } catch (InterruptedException ex) {
         Logger.getLogger(MessagePasser.class.getName()).log(Level.SEVERE, null, ex);
         }*/
        //now reach out to remaining nodes
        //iterate through list again to connect to all machines
        for (Map<String, Object> key : config) {
            //is this me? if it is, go through rest of list and send my own Host object to every other person on list with diff connect port, wait for them to connect back to me, send ping to say I am done
            //is this not me? then wait for this guy to connect to me and send me his Host object, connect to him based on what his host object said, store it, wait for ping to move on
            String name = (String) key.get("name");
            if (name.equals(localName)) {
                myturn = true;
            }
            if (myturn && !name.equals(localName)) {
                String ipAddr = (String) key.get("ip");
                int port = (Integer) key.get("port");
                try {
                    System.out.println(localName + " connecting to " + name);
                    Socket connection = new Socket(ipAddr, port);
                    if (connection.isConnected()) {
                        System.out.println(localName + " connection to " + name + " succeeded");
                    }
                    localhost.port = localport + hostcounter;
                    ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                    output.writeObject(localhost);
                    output.close();
                    connection.close();
                    System.out.println(localName + " waiting for reconnect from " + name);

                    Socket connection2 = (new ServerSocket(localport + hostcounter)).accept();
                    if (connection2.isConnected()) {
                        System.out.println(localName + " final connection to " + name + " succeeded");
                    }
                    Host host = new Host(name, new SocketHandler(connection2), ipAddr);
                    hostList.add(host);
                    hostcounter++;
                    System.out.println(localName + " added one host. " + hostList.size() + " hosts connected");

                } catch (IOException ex) {
                    Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else if (!name.equals(localName)) {
                /* try {
                 System.out.println(localName + " waiting for connection");
                 Socket connection = (new ServerSocket(localport)).accept();
                 System.out.println(localName + " got a new connection");
                 ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                 Host received = (Host) input.readObject();
                 System.out.println(localName + " received object from " + received.name);
                 input.close();
                 connection.close();
                 System.out.println(localName + " connecting to " + received.name + " over port " + received.port + " with address " + received.address);
                 Socket connection2 = new Socket(received.address, received.port);
                 if (connection2.isConnected()) {
                 System.out.println(localName + " final connection to " + received.name + " succeeded");
                 }
                 connection2.getInputStream().read(); //should block until DONEPING is received
                 received.sock = new SocketHandler(connection2);
                 hostList.add(received);
                 System.out.println(localName + " added one host. " + hostList.size() + " hosts connected");

                 } catch (IOException ex) {
                 Logger.getLogger(MessagePasser.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (ClassNotFoundException ex) {
                 Logger.getLogger(MessagePasser.class.getName()).log(Level.SEVERE, null, ex);
                 }*/
            }
        }
        System.out.println(localName + " done. " + hostList.size() + " hosts connected");

        //notify all other nodes that this one is done connecting to everyone
        /*for (int i = 0; i < hostList.size(); i++) {
         try {
         hostList.get(i).sock.sock.getOutputStream().write(1);
         } catch (IOException ex) {
         Logger.getLogger(MessagePasser.class.getName()).log(Level.SEVERE, null, ex);
         }
         }*/
        for (int i = 0; i < hostList.size(); i++) {
            hostList.get(i).sock.start();
        }
    }

    public Map<String, ArrayList<Map<String, Object>>> getYamlData(String pathName) {
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

	public void send(Message m) {
		// set message contents

		m.set_source(localSource);
		m.set_seqNum(seqNum);
		seqNum++;
		String src, dst, kind, action;
		int seq;


		System.out.println("Processing message from " +m.source +  " to " + m.dest + " " + m.kind + " " + m.sequenceNumber);
		for (int i = 0; i < hostList.size(); i++) {
			if (hostList.get(i).name.equals(m.dest)) {

				for (Rule rule : sendRules) {
					src = rule.src;
					dst = rule.dst;
					kind = rule.kind;
					action = rule.action;
					seq = rule.seq;
					if (m.source.equals(src) || src == null) {
						if (m.dest.equals(dst) || dst == null) {
							if (m.kind.equals(kind) || kind == null) {
								if (m.sequenceNumber == seq || seq == -1) {
									if (action.equals("drop")){
										System.out.println("dropped--------------------------------");
										return;
									}
									else if (action.equals("delay")){
										sendDelayQueue.add(m);
										System.out.println("delayed message " + m.data);
										System.out.println("delay----------------------------------------");
										return;
									}
									else if (action.equals("duplicate")) {
										System.out.println("duplicated-------------------------------");
										hostList.get(i).sock.send(m);
										m.dupe = true;
										hostList.get(i).sock.send(m);
										return;
									}
								}
							}
						}
					} 
				}
				hostList.get(i).sock.send(m);
				while (!sendDelayQueue.isEmpty()){
					Message delayed = sendDelayQueue.poll();
						for (int j = 0; j < hostList.size(); j++) {
							if (hostList.get(j).name.equals(m.dest)) {
						hostList.get(j).sock.send(delayed);
							
						}
					}

			}

			}
		}
	}
	
//    public Message receive() {
//        int i = 0;
//        while (i < hostList.size() && hostList.get(i).sock.receiveQueue.isEmpty()) {
//            i++;
//        }
//        if (i < hostList.size()) {
//            return hostList.get(i).sock.receiveQueue.poll();
//        } else {
//            return null;
//        }
//    }

    public Message receive() {
        int i = 0;
        String src, dst, kind, action;
		int seq;
        while (i < hostList.size() && hostList.get(i).sock.receiveQueue.isEmpty()) {
            i++;
        }
        if (i < hostList.size()) {
        	
            Message m =  hostList.get(i).sock.receiveQueue.poll();
    		System.out.println("Receiving message from " +m.source +  " to " + m.dest + " " + m.kind + " " + m.sequenceNumber);

            for (Rule rule : recvRules) {
				src = rule.src;
				dst = rule.dst;
				kind = rule.kind;
				action = rule.action;
				seq = rule.seq;
				if (m.source.equals(src) || src == null) {
					if (m.dest.equals(dst) || dst == null) {
						if (m.kind.equals(kind) || kind == null) {
							if (m.sequenceNumber == seq || seq == -1) {
								if (action.equals("drop")){
									System.out.println("dropped--------------------------------");
									return null;
								}
								else if (action.equals("delay")){
									System.out.println("delay----------------------------------------");
									recvDelayQueue.add(m);
									return null;
								}
								else if (action.equals("duplicate")) {
									System.out.println("duplicated-------------------------------");
									m.dupe = true;
									recvDelayQueue.add(m);
									return m;
								}
							}
						}
					}
				} 
					return m;
            }
        }
            return null;
        
    }
}

class SocketHandler implements Runnable {

	public Socket sock;
    public LinkedList<Message> receiveQueue;
    private Thread t;
    private ObjectOutputStream output = null;
    private ObjectInputStream input = null;

    public SocketHandler(String action, String host, int port) {
        try {
            if (action == "listen") {
                sock = (new ServerSocket(port)).accept();
            } else if (action == "bind") {
                sock = new Socket(host, port);
            } else {
                System.out.println("ERROR: Action given not valid");
            }

        } catch (IOException ex) {
            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public SocketHandler(Socket newsock) {
        sock = newsock;
            try {
                output = new ObjectOutputStream(sock.getOutputStream());
                input = new ObjectInputStream(sock.getInputStream());
            } catch (IOException ex) {
                Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        receiveQueue = new LinkedList<Message>();
    }

    public void send(Message m) {
        try {
            
            output.writeObject(m);
        } catch (IOException ex) {
            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void run() {
        try {
            
            while (true) {
                Message received = null;
                received = (Message) input.readObject();
                receiveQueue.add(received);
            }
        } catch (IOException ex) {
            System.out.println(ex.toString());
            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
                        System.out.println(ex.toString());

            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, "a");
            t.start();
        }
    }
}

class Host implements Serializable {

    private static final long serialVersionUID = 1529127835408294641L;
    public String name;
    public SocketHandler sock;
    public String address;
    public int port;

    public Host(String newname, SocketHandler newsock, String newaddress) {
        name = newname;
        sock = newsock;
        address = newaddress;
    }



}
class Rule {
	public String action = null;
	public String src = null;
	public String dst = null;
	public String kind = null;
	public int seq = -1;
	
	public Rule(String action, String src, String dst, String kind, int seq){
		this.action = action;
		this.src = src;
		this.dst = dst;
		this.kind = kind;
		this.seq = seq;
	}
}
