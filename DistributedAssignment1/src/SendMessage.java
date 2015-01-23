
public class SendMessage {
    public void send(Message m){
        //checkrules
        for(int i=0; i < hostList.length(); i++){
            if(hostList.get(i).name == m.dest){
                hostList.get(i).sock.send(m);
            }
        }
    }
}
