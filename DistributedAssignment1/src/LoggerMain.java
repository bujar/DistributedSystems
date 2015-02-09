import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerMain {

	public static void main(String[] args) {

		LoggerNode logger = new LoggerNode(
				"http://keanelucas.com/configuration.yml", "logger", "vector");
		logger.start();
	}
}

class LoggerNode implements Runnable {

	public MessagePasser msg;
	public String configfile;
	public String name;
	public String clockType;
	private Thread t;
	public ArrayList<TimeStampedMessage> loggedMessages = new ArrayList<TimeStampedMessage>();
	Map<TimeStampedMessage, ArrayList<TimeStampedMessage>> concurrentMap = new HashMap<TimeStampedMessage, ArrayList<TimeStampedMessage>>();

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
				if (tm.data.equals("showlogs")) {

					System.out.println("\n\nShowing current logged messages");
					for (int j = 0; j < loggedMessages.size(); j++) {
						ArrayList<TimeStampedMessage> temp = concurrentMap
								.get(loggedMessages.get(j));
						System.out.println(loggedMessages.get(j).stamp.value[0]
								+ "  EVENT: " + loggedMessages.get(j).source
								+ " " + loggedMessages.get(j).kind + " "
								+ loggedMessages.get(j).dest + " seq: "
								+ loggedMessages.get(j).sequenceNumber + "\t\""
								+ loggedMessages.get(j).data + "\"");
						if (!temp.isEmpty()) {
							System.out.println("\tConcurrent With:");
							for (TimeStampedMessage event : temp) {
								System.out.println("\t" + event.stamp.value[0]
										+ "," + event.stamp.value[1] + ","
										+ event.stamp.value[2] + ","
										+ event.stamp.value[3] + "  EVENT: "
										+ event.source + " " + event.kind + " "
										+ event.dest + " seq: "
										+ event.sequenceNumber + "\t\""
										+ event.data + "\"");

							}
						}
						System.out.println("\n");
					}
				} else {
					concurrentMap.put(tm, new ArrayList<TimeStampedMessage>());

					loggedMessages.add(tm);
					for (TimeStampedMessage msg : loggedMessages) {
						for (TimeStampedMessage entry : concurrentMap.keySet()) {
							if (entry.stamp.concurrentWith(msg.stamp)
									&& entry != msg)
								if (!concurrentMap.get(entry).contains(msg))
									concurrentMap.get(entry).add(msg);
						}
					}

					// bubble sort
					if (loggedMessages.size() > 1) {
						boolean flag = true;
						while (flag) {
							flag = false;
							for (int i = 0; i < loggedMessages.size() - 1; i++) {
								if (loggedMessages.get(i).stamp
										.happenedAfter(loggedMessages
												.get(i + 1).stamp)) {
									Collections.swap(loggedMessages, i, i + 1);
									flag = true;
								}
							}
						}

					}
				}
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				Logger.getLogger(LoggerNode.class.getName()).log(Level.SEVERE,
						null, ex);
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
