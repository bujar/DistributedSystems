
import java.io.Serializable;

public class Message implements Serializable {

    //Serial number needs to be the same in all instances. Randomly generated.
    private static final long serialVersionUID = 1529127835408294640L;
    public String dest;
    public String kind;
    public Object data;
    public String source;
    public int sequenceNumber;
    public Boolean dupe;

    public Message(String newdest, String newkind, Object newdata) {
        dest = newdest;
        kind = newkind;
        data = newdata;
    }

    public Message(Message m) {
        this.dest = m.dest;
        this.kind = m.kind;
        this.data = m.data;
        this.source = m.source;
        this.sequenceNumber = m.sequenceNumber;
        this.dupe = m.dupe;
    }

    public void set_source(String newsource) {
        source = newsource;
    }

    public void set_seqNum(int newsequenceNumber) {
        sequenceNumber = newsequenceNumber;
    }

    public Boolean getDupe() {
        return dupe;
    }

    public void set_duplicate(Boolean d) {
        dupe = d;
    }
}
