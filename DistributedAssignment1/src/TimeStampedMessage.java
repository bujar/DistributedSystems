
import java.io.Serializable;

public class TimeStampedMessage extends Message implements Serializable {

    TimeStamp stamp;
    public TimeStampedMessage(String newdest, String newkind, Object newdata, TimeStamp newstamp) {
        super(newdest, newkind, newdata);
        stamp = newstamp;
    }
    
    public TimeStampedMessage(Message m, TimeStamp newstamp) {
        super(m.dest, m.kind, m.data);
        stamp = newstamp;
    }
    
    public void setTimeStamp(TimeStamp newstamp){
        stamp = newstamp;
    }
    
    public Message getMessage(){
        return new Message(dest, kind, data);
    }
}
