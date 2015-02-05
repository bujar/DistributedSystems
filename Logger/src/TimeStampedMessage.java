
import java.io.Serializable;

public class TimeStampedMessage extends Message implements Serializable {

    private static final long serialVersionUID = 1529127835408294640L;
    TimeStamp stamp;

    public TimeStampedMessage(String newdest, String newkind, Object newdata, TimeStamp newstamp) {
        super(newdest, newkind, newdata);
        stamp = newstamp;
    }

    public TimeStampedMessage(Message m, TimeStamp newstamp) {
        super(m);
        stamp = newstamp;
    }

    public void setTimeStamp(TimeStamp newstamp) {
        stamp = newstamp;
    }

    public Message getMessage() {
        return (Message) this;
    }
}
