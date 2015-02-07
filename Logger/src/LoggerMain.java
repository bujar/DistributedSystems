
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerMain {
    public static void main(String[] args) {

        LoggerNode logger = new LoggerNode("configuration.yml", "logger", "vector");
        logger.start();
    }
}

class LoggerNode implements Runnable {

    public MessagePasser msg;
    public String configfile;
    public String name;
    public String clockType;
    private Thread t;
    public ArrayList <TimeStampedMessage> loggedMessages = new ArrayList<TimeStampedMessage>();

    public LoggerNode(String newconfigfile, String newname, String newclockType) {
        configfile = newconfigfile;
        name = newname;
        clockType = newclockType;
    }

    public void run() {
        msg = new MessagePasser(configfile, name, clockType);

        while (true) {
            TimeStampedMessage tm = msg.receiveWithTimeStamp();
            if (tm != null) {                
                if (tm.data.equals("showlogs")){
                	System.out.println("Showing current logged messages");
                	for (TimeStampedMessage log : loggedMessages)
                		System.out.println("EVENT: " + log.source +" "+ log.kind + " " + log.dest + " seq: " + log.sequenceNumber + "\t\"" + log.data + "\"");                    
                	}
                else
                	loggedMessages.add(tm);
                }

                //bubble sort
                if (loggedMessages.size()>1){
				boolean flag = true;
				while (flag) {
					flag = false;
					for (int i = 0; i < loggedMessages.size()-1; i++) {
						if (loggedMessages.get(i).stamp.happenedAfter(loggedMessages.get(i+1).stamp)){
						Collections.swap(loggedMessages, i, i + 1);
						flag=true;
						}
					}
				}
	            
			}
 

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LoggerNode.class.getName()).log(Level.SEVERE, null, ex);
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
