
import java.io.Serializable;
import java.util.ArrayList;

public class MulticastMessage extends TimeStampedMessage implements Serializable {

    private static final long serialVersionUID = 1529127835408294642L;
    Group group;
    long timeReceived = 0;
    ArrayList<TimeStampedMessage> acksReceived;
    
    public MulticastMessage(String newdest, String newkind, Object newdata, TimeStamp newstamp, Group newgroup) {
        super(newdest, newkind, newdata, newstamp);
        group = newgroup;
        timeReceived = System.currentTimeMillis();
        acksReceived = new ArrayList<TimeStampedMessage>();
    }

    public MulticastMessage(TimeStampedMessage m, Group newgroup) {
        super(m.getMessage(),m.stamp);
        group = newgroup;
        timeReceived = System.currentTimeMillis();
    }
    
    //adds ack to acklist
    public void addAck(TimeStampedMessage newack){
        for(int i = 0; i < acksReceived.size(); i++){
            if(acksReceived.get(i).source.equals(newack.source)){
               return; 
            }
        }
        acksReceived.add(newack);
    }
    
    //will return true if the number of acks received equals the number of members in the group (means it also inludes own ack if self is in group)
    public boolean fullyAcked(){
        return acksReceived.size() == group.members.size()-1;
    }
    
    //returns time passes since this message was received. will be used to determine if Ack has been missing too long from another node
    public long getTimePassed(long currentTime){
        return currentTime-timeReceived;
    }
}
