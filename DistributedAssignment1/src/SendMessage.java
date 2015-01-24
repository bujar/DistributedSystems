
import java.util.Set;


public class SendMessage {
    public int seqNum;
    public void send(Message m){
        
        //set message contents
        m.set_seqNum(seqNum);
        seqNum++;
        for(int i=0; i < hostList.length(); i++){
            if(hostList.get(i).name == m.dest){
                hostList.get(i).sock.send(m);
            }
        }
    }
}
