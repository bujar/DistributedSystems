
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {

        //will change to retrieving paramaters from user args
        testNode alice = new testNode("configuration.yml", "alice");
        testNode bob = new testNode("configuration.yml", "bob");
        testNode charlie = new testNode("configuration.yml", "charlie");
        testNode daphne = new testNode("configuration.yml", "daphne");
        alice.start();
        bob.start();
        charlie.start();
        daphne.start();

    }
}

class testNode implements Runnable {

    public MessagePasser msg;
    public String configfile;
    public String name;
    private Thread t;

    public testNode(String newconfigfile, String newname) {
        configfile = newconfigfile;
        name = newname;
    }

    public void run() {
        msg = new MessagePasser(configfile, name);
        if (name.equals("alice")) {
            msg.send(new Message("bob", "MX", "bla"));
            msg.send(new Message("bob", "MX", "bla2"));
            msg.send(new Message("charlie", "MX", "blatocharlie"));
            msg.send(new Message("daphne", "MX", "blatodapne"));
            //System.out.println(msg.receive().data);
        } else if (name.equals("bob")) {
            msg.send(new Message("alice", "MX", "bla"));
            msg.send(new Message("charlie", "MX", "alltheblastocharliefrombob"));
            //System.out.println(msg.receive().data);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(testNode.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (true) {
            Message m = msg.receive();
            if (m != null) {
                System.out.println(name + " received: " + m.data);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(testNode.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, "MsgPsr:" + name);
            t.start();
        }
    }
}
