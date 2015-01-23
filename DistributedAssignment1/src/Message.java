
public class Message {
    public String dest;
    public String kind;
    public Object data;
    public String source;
    public int seqnum;
    public Message(String newdest, String newkind, Object newdata){
        dest = newdest;
        kind = newkind;
        data = newdata;
    }
    
    public void set_source(String newsource){
        
    }
    
    public void set_seqNum(int newsequenceNumber){
        
    }
    public void set_duplicate(Boolean newdupe){
        
    }
}
