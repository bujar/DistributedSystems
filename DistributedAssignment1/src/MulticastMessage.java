
import java.io.Serializable;
import java.util.ArrayList;

public class MulticastMessage extends TimeStampedMessage implements Serializable {

    private static final long serialVersionUID = 1529127835408294642L;
    Group group;
    long timeReceived = 0;
    TimeStamp globalStamp;
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
        acksReceived = new ArrayList<TimeStampedMessage>();
    }
    
    //adds ack to acklist
    public void addAck(MulticastMessage newack){
        if(!group.members.contains(newack.source) || !group.name.equals(newack.group.name)){
            return;
        }
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
    
    public String getMissing(String localSource){
        if(fullyAcked()){
            return null;
        }
        for(String member : group.members){
            boolean hasAcked = false;
            for(TimeStampedMessage ack : acksReceived){
                if(member.equals(ack.source) && !member.equals(localSource)){
                    hasAcked = true;
                }
            }
            if(!hasAcked){
                return member;
            }
        }
        return null;
    }
    
    //returns time passes since this message was received. will be used to determine if Ack has been missing too long from another node
    public long getTimePassed(long currentTime){
        return currentTime-timeReceived;
    }
}
