
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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.yaml.snakeyaml.Yaml;

public class MessagePasser {
    //done

    public static ArrayList<Host> hostList = new ArrayList<Host>();
    public static int seqNum;
    Queue<Message> recvDelayQueue;
    Queue<Message> sendDelayQueue;

    public MessagePasser(String pathName, String localName) {
        seqNum = 0;

        Map<String, ArrayList<Map<String, Object>>> data = getYamlData(pathName);
        ArrayList<Map<String, Object>> config = data.get("configuration");

        String action = "bind";
        Integer localport = 0;
        for (Map<String, Object> key : config) {

            String name = (String) key.get("name");
            if(name == localName){
                action = "listen";
                int port = (Integer) key.get("port");
                localport = Integer.valueOf(port);
            }
            if(action == "listen"){
                String ipAddr = (String) key.get("ip");
                SocketHandler connection = new SocketHandler(action, "0.0.0.0", localport);
                Host host = new Host(name, connection, ipAddr);
                hostList.add(host);
            }else{
                String ipAddr = (String) key.get("ip");
                int port = (Integer) key.get("port");
                p = Integer.valueOf(port);
                SocketHandler connection = new SocketHandler(action, ipAddr, p);
                Host host = new Host(name, connection, ipAddr);
                hostList.add(host);
            }

        }
        System.out.println(hostList);
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
            if (hostList.get(i).name == m.dest) {
                hostList.get(i).socketHandler.send(m);
            }
        }
    }
}

class SocketHandler implements Runnable{
  private Socket sock;
  Queue<Message> receiveQueue;
  DataOutputStream out;
  DataInputStream in;
 
  public SocketHandler(String action, String host, int port)
  {
      try {
          if(action == "listen"){
              sock = (new ServerSocket(port)).accept();
          }else if(action == "bind"){
            sock = new Socket(host, port);
          }else{
              System.out.println("ERROR: Action given not valid");
          }
          out = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
          in = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
          out.writeInt(port);
          
      } catch (IOException ex) {
          Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
      }
  }
 public SocketHandler(Socket newsock){
     sock = newsock;
 }

  public void send(Message m){
      sock.getOutputStream().write(m.);
  }
  public void run()
  {
    
  }
}

class Host {
    public String name;
    public SocketHandler sock;
    public String address;
    
    public Host(String newname, SocketHandler newsock, String newaddress){
        name = newname;
        sock = newsock;
        address = newaddress;
    }
   
}
