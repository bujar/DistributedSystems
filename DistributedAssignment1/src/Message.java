
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


    /*
     * SendMessage Implementation
     * 
     * private ObjectInputStream input = null;
     * private ObjectOutpputStream output = null;
     * 
     * 
     * ....
     * SEND()
     * Message m;
     * output = new ObjectOutputStream(socket.getOutputStream());
     * output.writeObject(m);
     * 
     * 
     * RECEIVE()
     * input = new ObjectInputStream(socket.getInputStream());
     * Message received = (Message) input.readObject();
     * 
     */
}
