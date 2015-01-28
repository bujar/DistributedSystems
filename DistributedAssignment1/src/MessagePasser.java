
import java.awt.List;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.yaml.snakeyaml.Yaml;

public class MessagePasser {
    //done

    public ArrayList<Host> hostList = new ArrayList<Host>();
    public int seqNum;
    Queue<Message> recvDelayQueue;
    Queue<Message> sendDelayQueue;

    public MessagePasser(String pathName, String localName) {
        seqNum = 0;

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
        //listen first
        if (listencounter > 0) {
            try {
                System.out.println(localName + " waiting for connection");
                Socket connection = (new ServerSocket(localport)).accept();
                                    ObjectInputStream input = new ObjectInputStream(connection.getInputStream());

                while (hostList.size() < listencounter) {
                    if (connection.isClosed()) {
                        connection = (new ServerSocket(localport)).accept();
                    }
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
        try {
            Thread.sleep(5000);
            System.out.println(localName+"'s TURN!!!!!");
        } catch (InterruptedException ex) {
            Logger.getLogger(MessagePasser.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        //set message contents
        m.set_seqNum(seqNum);
        seqNum++;
        for (int i = 0; i < hostList.size(); i++) {
            if (hostList.get(i).name.equals(m.dest)) {
                hostList.get(i).sock.send(m);
            }
        }
    }

    public Message receive() {
        int i = 0;
        while (i < hostList.size() && hostList.get(i).sock.receiveQueue.isEmpty()) {
            i++;
        }
        if (i < hostList.size()) {
            return hostList.get(i).sock.receiveQueue.poll();
        } else {
            return null;
        }
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
        receiveQueue = new LinkedList<Message>();
    }

    public void send(Message m) {

        try {
            output = new ObjectOutputStream(sock.getOutputStream());
            output.writeObject(m);
        } catch (IOException ex) {
            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void run() {
        Message received = null;
        try {
            input = new ObjectInputStream(sock.getInputStream());
            received = (Message) input.readObject();
        } catch (IOException ex) {
            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        receiveQueue.add(received);
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
