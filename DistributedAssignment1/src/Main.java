
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {

        //will change to retrieving paramaters from user args
        //testNode alice = new testNode("configuration.yml", "alice");
        //testNode bob = new testNode("configuration.yml", "bob");
        testNode charlie = new testNode("configuration.yml", "charlie");
        testNode daphne = new testNode("configuration.yml", "daphne");
        //alice.start();
        //bob.start();
        charlie.start();
        daphne.start();
        
        Scanner input = new Scanner(System.in);
        String command = input.nextLine();
        while(true){
            Scanner parse = new Scanner(command);
            String name = parse.next();
            if(name.equals("alice")){
          //      alice.msg.send(new Message(parse.next(),parse.next(),parse.next()));
            }else if(name.equals("bob")){
            //    bob.msg.send(new Message(parse.next(),parse.next(),parse.next()));
            }else if(name.equals("charlie")){
                charlie.msg.send(new Message(parse.next(),parse.next(),parse.next()));
            }else if(name.equals("daphne")){
                daphne.msg.send(new Message(parse.next(),parse.next(),parse.next()));
            }
            command = input.nextLine();
        }
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
        /*if (name.equals("alice")) {
            msg.send(new Message("bob", "MX", "bla"));
//            msg.send(new Message("bob", "MX", "bla2"));
            msg.send(new Message("charlie", "MX", "blatocharlie"));
            msg.send(new Message("daphne", "MX", "blatodapne"));
            //System.out.println(msg.receive().data);
        } else if (name.equals("bob")) {
            msg.send(new Message("charlie", "Lookup", "alltheblastocharliefrombob"));
            msg.send(new Message("charlie", "Lookup", "another message to charlie"));


            msg.send(new Message("alice", "Ack", "blaaaaaaaaaaa"));
            //System.out.println(msg.receive().data);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(testNode.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        while (true) {
            Message m = msg.receive();
            if (m != null) {
                System.out.println(name + " received: " + m.data);
            }
            Message delayed = null;
            while (!MessagePasser.recvDelayQueue.isEmpty()){
            	delayed = MessagePasser.recvDelayQueue.poll();
            	System.out.println(delayed.data);
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
