
public class Message {
    public String dest;
    public String kind;
    public Object data;
    public String source;
    public int sequenceNumber;
    public Boolean dupe;
    public Message(String newdest, String newkind, Object newdata){
        dest = newdest;
        kind = newkind;
        data = newdata;
    }
    
    public void set_source(String newsource){
        source = newsource;
    }
    
    public void set_seqNum(int newsequenceNumber){
        sequenceNumber = newsequenceNumber;
    }
    public void set_duplicate(Boolean newdupe){
        dupe = newdupe;
    }
}
