
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String command = input.nextLine();
        Scanner parse = new Scanner(command);
        //will change to retrieving paramaters from user args
    //    testNode alice = new testNode("configuration.yml", "alice", "logical");
   //     testNode bob = new testNode("configuration.yml", "bob", "logical");
        testNode node = new testNode("configuration.yml", parse.next(), parse.next());
  //      testNode daphne = new testNode("configuration.yml", "daphne", "logical");
//        alice.start();
//        bob.start();
        node.start();
 //       daphne.start();

        
        
        while (true) {
            command = input.nextLine();
            parse = new Scanner(command);
            String name = parse.next();
            if (name.equals("showlogs")){
            	node.msg.send(new TimeStampedMessage("logger", "showlogs", "showlogs", null));	//using alice.msg.send - will change later  
            }
            else{
            String arg1 = parse.next();
            String arg2 = parse.nextLine();
                node.msg.send(new Message(name, arg1, arg2));
            }
        }
    }
}

class testNode implements Runnable {

    public MessagePasser msg;
    public String configfile;
    public String name;
    public String clockType;
    private Thread t;

    public testNode(String newconfigfile, String newname, String newclockType) {
        configfile = newconfigfile;
        name = newname;
        clockType = newclockType;
    }

    public void run() {
        msg = new MessagePasser(configfile, name, clockType);
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
//        while (true) {
//            Message m = msg.receive();
//            if (m != null) {
//                System.out.println(name + " received: " + m.data);
//            }
        while (true) {
            TimeStampedMessage tm = msg.receiveWithTimeStamp();
            if (tm != null) {
            	if (msg.logAllMessages)
            		msg.sendToLogger(tm);
                System.out.println(name + " received: " + tm.data);
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
