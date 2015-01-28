
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
       /* if (name.equals("alice")) {
            msg.send(new Message("bob", "MX", "bla"));
            System.out.println(msg.receive().data);
        } else if (name.equals("bob")) {
            msg.send(new Message("alice", "MX", "bla"));
            System.out.println(msg.receive().data);
        }*/

    }

    public void start() {
        if (t == null) {
            t = new Thread(this, "MsgPsr:" + name);
            t.start();
        }
    }
}
