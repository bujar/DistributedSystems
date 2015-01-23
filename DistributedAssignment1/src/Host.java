
import java.net.Socket;

public class Host {
    public String name;
    public Socket sock;
    public String address;
    
    public Host(String newname, Socket newsock, String newaddress){
        name = newname;
        sock = newsock;
        address = newaddress;
    }
   
}
