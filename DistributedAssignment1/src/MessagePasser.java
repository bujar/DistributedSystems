
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static java.lang.Math.max;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yaml.snakeyaml.Yaml;

public class MessagePasser {
    // done

    public ArrayList<Host> hostList = new ArrayList<Host>();
    public ArrayList<Group> groupList = new ArrayList<Group>();
    public ArrayList<Rule> sendRules = new ArrayList<Rule>();
    public ArrayList<Rule> recvRules = new ArrayList<Rule>();
    public int seqNum;
    public LinkedList<MulticastMessage> recvDelayQueue;
    public LinkedList<TimeStampedMessage> sendDelayQueue;
    public static String configFile;
    public static String configURL;
    public String localSource;
    public boolean delayed;
    public ClockService clock;
    public boolean logAllMessages;
    public LinkedList<MulticastMessage> multicastQueue;
    public LinkedList<MulticastMessage> pushedMulticastQueue;

    public MessagePasser(String pathName, String localName, String clockType) {

        delayed = false;
        seqNum = 0;
        configURL = pathName;
        configFile = "configuration.yml";
        localSource = localName;
        sendDelayQueue = new LinkedList<TimeStampedMessage>();
        recvDelayQueue = new LinkedList<MulticastMessage>();
        multicastQueue = new LinkedList<MulticastMessage>();
        pushedMulticastQueue = new LinkedList<MulticastMessage>();
        checkForUpdate();
        Map<String, ArrayList<Map<String, Object>>> data = getYamlData(configFile);
        ArrayList<Map<String, Object>> groups = data.get("groups");
        // iterate through config file to find own info
        for (Map<String, Object> key : groups) {
            String groupName = (String) key.get("name");
            ArrayList members = (ArrayList) key.get("members");
            Group newGroup = new Group(groupName, members);
            groupList.add(newGroup);
        }

        ArrayList<Map<String, Object>> config = data.get("configuration");

        Integer localport = 0;
        Host localhost = null;
        boolean myturn = false;
        int hostcounter = 1;

        int listencounter = 0;
        int totalnodes = 0;

        // iterate through config file to find own info
        for (Map<String, Object> key : config) {
            String name = (String) key.get("name");
            if (name.equals(localName)) {
                String ipAddr = (String) key.get("ip");
                int port = (Integer) key.get("port");
                localport = Integer.valueOf(port);
                localhost = new Host(name, null, ipAddr);
                listencounter = totalnodes;
            }
            totalnodes++;
        }

        // intiialize clock
        if (clockType.equals("logical")) {
            clock = new LogicalClock();
        } else if (clockType.equals("vector")) {
            clock = new VectorClock(totalnodes, listencounter);
        } else {
            System.out.println(clockType + " not a valid Clock option.");
        }

        // listen first
        if (listencounter > 0) {
            try {
                // System.out.println(localName + " waiting for connection");
                ServerSocket server = (new ServerSocket(localport));

                while (hostList.size() < listencounter) {
                    Socket connection = server.accept();
                    ObjectInputStream input = new ObjectInputStream(
                            connection.getInputStream());
                    // System.out.println(localName + " got a new connection");
                    // do {
                    Host received = (Host) input.readObject();
					// System.out.println(localName + " received object from " +
                    // received.name);

                    // System.out.println(localName + " connecting to " +
                    // received.name + " over port " + received.port +
                    // " with address " + received.address);
                    Socket connection2 = new Socket(received.address,
                            received.port);
                    if (connection2.isConnected()) {
                        // System.out.println(localName +
                        // " final connection to " + received.name +
                        // " succeeded");
                    }
                    // connection2.getInputStream().read(); //should block until
                    // DONEPING is received
                    received.sock = new SocketHandler(connection2, clock);
                    hostList.add(received);
                    connection.close();
                    // System.out.println(localName + " added one host. " +
                    // hostList.size() + " hosts connected");
                    // } while (input.available() > 0);
                }

            } catch (IOException ex) {
                System.out.println(localName + " error listening "
                        + ex.toString());
                Logger.getLogger(MessagePasser.class.getName()).log(
                        Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                System.out.println(localName + " error listening "
                        + ex.toString());
                Logger.getLogger(MessagePasser.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }

        // now reach out to remaining nodes
        // iterate through list again to connect to all machines
        for (Map<String, Object> key : config) {
            boolean connectionmade = false;
            while (!connectionmade) {
                connectionmade = true;
                // is this me? if it is, go through rest of list and send my own
                // Host object to every other person on list with diff connect
                // port,
                // wait for them to connect back to me, send ping to say I am
                // done
                // is this not me? then wait for this guy to connect to me and
                // send
                // me his Host object, connect to him based on what his host
                // object
                // said, store it, wait for ping to move on
                String name = (String) key.get("name");
                if (name.equals(localName)) {
                    myturn = true;
                }
                if (myturn && !name.equals(localName)) {
                    String ipAddr = (String) key.get("ip");
                    int port = (Integer) key.get("port");
                    try {
                        // System.out.println(localName + " connecting to " +
                        // name);
                        Socket connection = new Socket(ipAddr, port);
                        if (connection.isConnected()) {
                            // System.out.println(localName + " connection to "
                            // +
                            // name + " succeeded");
                        }
                        localhost.port = localport + hostcounter;
                        ObjectOutputStream output = new ObjectOutputStream(
                                connection.getOutputStream());
                        output.writeObject(localhost);
                        output.close();
                        connection.close();
                        // System.out.println(localName +
                        // " waiting for reconnect from " + name);

                        Socket connection2 = (new ServerSocket(localport
                                + hostcounter)).accept();
                        if (connection2.isConnected()) {
                            // System.out.println(localName +
                            // " final connection to " + name + " succeeded");
                        }
                        Host host = new Host(name, new SocketHandler(
                                connection2, clock), ipAddr);
                        hostList.add(host);
                        hostcounter++;
                        // System.out.println(localName + " added one host. " +
                        // hostList.size() + " hosts connected");
                    } catch (IOException ex) {
                        try {
                            connectionmade = false;
                            Thread.sleep(1000);
                        } catch (InterruptedException ex1) {
                            Logger.getLogger(MessagePasser.class.getName())
                                    .log(Level.SEVERE, null, ex1);
                        }
                        // Logger.getLogger(SocketHandler.class.getName()).log(
                        // Level.SEVERE, null, ex);
                    }

                }
            }
        }
        System.out.println(localName + " done. " + hostList.size()
                + " hosts connected");

        for (int i = 0; i < hostList.size(); i++) {
            hostList.get(i).sock.start();
        }
    }

    public void checkForUpdate() {
        // parse rules
        // URL url;
        // try {
        // url = new URL(configURL);
        // ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        // FileOutputStream fos = new FileOutputStream(configFile);
        // fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        // fos.close();
        // } catch (Exception e) {
        // System.out.println("error when accessing URL");
        // }
        sendRules.clear();
        recvRules.clear();

        Map<String, ArrayList<Map<String, Object>>> data = getYamlData(configFile);
        ArrayList<Map<String, Object>> checkLog = data.get("configuration");
        for (Map<String, Object> key : checkLog) {
            String name = (String) key.get("name");
            if (name.equals("logger")) {
                logAllMessages = (Boolean) key.get("logAllMessages");
            }
        }

        ArrayList<Map<String, Object>> sendRule = data.get("sendRules");
        for (Map<String, Object> key : sendRule) {
            String a = (String) key.get("action");
            String src = (String) key.get("src");
            String dst = (String) key.get("dest");
            String kind = (String) key.get("kind");
            boolean duplicate = false;
            if (key.get("duplicate") != null) {
                duplicate = (Boolean) key.get("duplicate");
            }
            int seq = -1;
            if (key.get("seqNum") != null) {
                seq = (Integer) key.get("seqNum");
            }
            Rule rule = new Rule(a, src, dst, kind, seq, duplicate);
            sendRules.add(rule);
        }
        ArrayList<Map<String, Object>> recvRule = data.get("receiveRules");
        for (Map<String, Object> key : recvRule) {
            String a = (String) key.get("action");
            String src = (String) key.get("src");
            String dst = (String) key.get("dest");
            String kind = (String) key.get("kind");
            boolean duplicate = false;
            if (key.get("duplicate") != null) {
                duplicate = (Boolean) key.get("duplicate");
            }
            int seq = -1;
            if (key.get("seqNum") != null) {
                seq = (Integer) key.get("seqNum");
            }
            Rule rule = new Rule(a, src, dst, kind, seq, duplicate);
            recvRules.add(rule);
        }
    }

    public Map<String, ArrayList<Map<String, Object>>> getYamlData(
            String pathName) {
        InputStream input = null;
        try {
            input = new FileInputStream(new File(pathName));
        } catch (FileNotFoundException e) {
            System.out.println("Could not find configuration file");
            e.printStackTrace();
        }
        Yaml yaml = new Yaml();
        Map<String, ArrayList<Map<String, Object>>> configData = (Map<String, ArrayList<Map<String, Object>>>) yaml
                .load(input);
        return configData;
    }

    public void send(Message m) {
        // set message contents
        // File config = new File(configFile);
        // if (config.lastModified() != lastModified) {
        checkForUpdate();
        // }
        m.set_source(localSource);
        m.set_duplicate(false);
        String src, dst, kind, action;
        int seq;

        // System.out.println("Processing message from " +m.source + " to " +
        // m.dest + " " + m.kind + " " + m.sequenceNumber);
        for (int i = 0; i < hostList.size(); i++) {
            if (hostList.get(i).name.equals(m.dest)) {

                for (Rule rule : sendRules) {
                    src = rule.src;
                    dst = rule.dst;
                    kind = rule.kind;
                    action = rule.action;
                    seq = rule.seq;
                    if (m.source.equals(src) || src == null) {
                        if (m.dest.equals(dst) || dst == null) {
                            if (m.kind.equals(kind) || kind == null) {
                                if (m.sequenceNumber == seq || seq == -1) {
                                    if (action.equals("drop")) {
                                        System.out
                                                .println("dropped--------------------------------");
                                        return;
                                    } else if (action.equals("delay")) {
                                        sendDelayQueue
                                                .add(new MulticastMessage(
                                                                new TimeStampedMessage(
                                                                        m,
                                                                        clock.getTimestamp()),
                                                                null));
                                        // System.out.println("delayed message "
                                        // + m.data);
                                        System.out
                                                .println("delay----------------------------------------");
                                        return;
                                    } else if (action.equals("duplicate")) {
                                        System.out
                                                .println("duplicated-------------------------------");
                                        hostList.get(i).sock
                                                .send(new MulticastMessage(
                                                                new TimeStampedMessage(
                                                                        m,
                                                                        clock.getTimestamp()),
                                                                null));
                                        Message copy = new Message(m.dest,
                                                m.kind, m.data);
                                        copy.set_source(localSource);
                                        copy.set_seqNum(seqNum - 1);
                                        copy.set_duplicate(true);
                                        hostList.get(i).sock
                                                .send(new MulticastMessage(
                                                                new TimeStampedMessage(
                                                                        copy,
                                                                        clock.getTimestamp()),
                                                                null));
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
                m.set_seqNum(seqNum);
                seqNum++;
                TimeStampedMessage tm = new TimeStampedMessage(m,
                        clock.getTimestamp());
                MulticastMessage multiMsg = new MulticastMessage(tm, null);
                hostList.get(i).sock.send(multiMsg);
                while (!sendDelayQueue.isEmpty()) {
                    TimeStampedMessage delayedMessage = sendDelayQueue.poll();
                    delayedMessage.set_seqNum(seqNum);
                    seqNum++;
                    for (int j = 0; j < hostList.size(); j++) {
                        if (hostList.get(j).name.equals(delayedMessage.dest)) {
                            hostList.get(j).sock.send(delayedMessage);

                        }
                    }

                }

            }
        }
    }

    public void sendMulticast(String groupName, String kind, String message,
            int sequenceNumber) {
        Group group = null;
        MulticastMessage m = null;

        for (Group g : groupList) {
            if (groupName.equals(g.name)) {
                group = g;
            }
        }

        if (group == null) {
            return;
        }
        TimeStamp currentTime = clock.getTimestamp();
        // send to all members of group
        for (String member : group.members) {
            m = new MulticastMessage(member, kind, message,
                    clock.getTimestamp(), group);
            m.globalStamp = currentTime;
            
            m.set_source(localSource);
            // if it is an ack message
            if (sequenceNumber != -1) {
                m.set_seqNum(sequenceNumber);
            } // regular multicast message
            else {
                m.set_seqNum(seqNum);
            }
            for (int i = 0; i < hostList.size(); i++) {
                if (hostList.get(i).name.equals(m.dest)) {
                    hostList.get(i).sock.send(m);

                }
            }
            if (m.dest.equals(localSource) && !m.kind.equals("ACK")) {
                hostList.get(0).sock.receiveQueue.add(m);
            }
        }
        seqNum++;
    }

    public Message receive() {

        TimeStampedMessage tm = receiveWithTimeStamp();
        if (tm != null) {
            return tm.getMessage();
        }
        return null;
    }

    public TimeStampedMessage receiveWithTimeStamp() {
        MulticastMessage mm = receiveWithMulticast();
        return (TimeStampedMessage) mm;
    }

    public MulticastMessage receiveWithMulticast() {
        if (!multicastQueue.isEmpty()) {
            sortMulticastQueue();
            MulticastMessage mm = multicastQueue.getFirst();
            if (mm.fullyAcked()) {
                mm.acksReceived.clear();
                if (!pushedMulticastQueue.contains(mm)) {
                    System.out.println("DEBUG: Fully acked, sending to app");
                    multicastQueue.remove(mm);
                    pushedMulticastQueue.add(mm);
                    return mm;
                }
                System.out.println("DUP Multicast found. removing.");
                multicastQueue.remove(mm);

            } else {
                if (mm.getTimePassed(System.currentTimeMillis()) > 2000) {
                    mm.timeReceived = System.currentTimeMillis();
                    String missing = mm.getMissing(localSource);
                    if (missing != null && !missing.equals(localSource) && mm.source == localSource) {
                        System.out.println("DEBUG: " + missing + " has not ACKED. Resending...");
                        for (Host host : hostList) {
                            if (host.name.equals(missing)) {
                                MulticastMessage mm2 = new MulticastMessage(mm, mm.group);
                                mm2.dest = missing;
                                mm2.source = localSource;
                                MulticastMessage localack = new MulticastMessage(missing, "ACK", "ACK",
                                        clock.getTimestamp(), mm2.group);
                                localack.set_source(localSource);
                                localack.set_seqNum(mm2.sequenceNumber);
                                MulticastMessage localack2 = new MulticastMessage(missing, "ACK", "ACK",
                                        clock.getTimestamp(), mm2.group);
                                localack2.set_source(missing);
                                localack2.set_seqNum(mm2.sequenceNumber);
                                mm2.acksReceived = mm.acksReceived;
                                mm2.acksReceived.add(localack);
                                host.sock.send(mm2);
                                mm2.acksReceived.remove(localack);
                                mm2.acksReceived.remove(localack2);
                            }
                        }
                    }
                }
            }
        }
        int i = 0;
        String src, dst, kind, action;
        boolean duplicate;
        MulticastMessage m = null;
        int seq;
        // File config = new File(configFile);
        // if (config.lastModified() != lastModified) {
        // checkForUpdate();
        // }
        while (i < hostList.size()
                && hostList.get(i).sock.receiveQueue.isEmpty()) {
            i++;
        }
        if (i < hostList.size()) {

            m = hostList.get(i).sock.receiveQueue.poll();
            System.out.println("DEBUG: Receiving message from " + m.source
                    + " to " + m.dest + " " + m.kind + " with sequenceNum: "
                    + m.sequenceNumber);

            checkForUpdate();
            for (Rule rule : recvRules) {
                src = rule.src;
                dst = rule.dst;
                kind = rule.kind;
                action = rule.action;
                seq = rule.seq;
                duplicate = rule.duplicate;

                if (m.source.equals(src) || src == null) {
                    if (m.dest.equals(dst) || dst == null) {
                        if (m.kind.equals(kind) || kind == null) {
                            if (m.sequenceNumber == seq || seq == -1) {
                                //  if (m.getDupe() != null
                                //        && m.getDupe() == duplicate) {
                                if (action.equals("drop")) {
                                    System.out
                                            .println("dropped--------------------------------");
                                    return receiveWithMulticast();
                                } else if (action.equals("delay")) {
                                    System.out
                                            .println("delay----------------------------------------");
                          //          if (!recvDelayQueue.contains(m)) {
                                        recvDelayQueue.add(m);
                            //        }
                                    delayed = true;
                                    return receiveWithMulticast();
                                } else if (action.equals("duplicate")) {
                                    System.out
                                            .println("duplicated-------------------------------");
                                    m.set_duplicate(true);
                                    recvDelayQueue.add(m);
                                    delayed = false;
                                    break;
                                }
                                //  }
                            }
                        }
                    }
                }
                delayed = false;
                clock.updateTimeStamp(m.stamp);
            }
        }
        if (!delayed && m == null) {
            m = recvDelayQueue.poll();
            if (m != null) {
                clock.updateTimeStamp(m.stamp);
            }
        }

        // if its a multicast message
        if (m != null && m.group != null) {
            if (m.kind.equals("ACK")) {
                for (MulticastMessage q : multicastQueue) {
                    System.out.println(m.sequenceNumber + " : " + q.sequenceNumber);
                    if (m.sequenceNumber == q.sequenceNumber) {
                        System.out.println("DEBUG: Received, adding to queue");
                        q.addAck(m);
                    }
                    return receiveWithMulticast();
                }
            } else {
                boolean dupmulti = false;
                for (MulticastMessage q : multicastQueue) {
                    if (m.sequenceNumber == q.sequenceNumber && m.group.name.equals(q.group.name)) {
                        dupmulti = true;
                    }
                }
                if (!dupmulti) {
                    m.timeReceived = System.currentTimeMillis();
                    multicastQueue.add(m);
                }
                System.out.println("DEBUG: Sending ACK message");
                sendAck(m.sequenceNumber, m.group);
            }
            return receiveWithMulticast();
        }
        return m;
    }

    private void sortMulticastQueue() {
        if (multicastQueue.size() > 1) {
            boolean flag = true;
            while (flag) {
                flag = false;
                for (int i = 0; i < multicastQueue.size() - 1; i++) {
                    if (multicastQueue.get(i).globalStamp
                            .happenedAfter(multicastQueue
                                    .get(i + 1).globalStamp)) {
                        Collections.swap(multicastQueue, i, i + 1);
                        flag = true;
                    }
                }
            }

        }
    }

    private void sendAck(int sequenceNumber, Group group) {
        sendMulticast(group.name, "ACK", "ACK", sequenceNumber);

    }

    public TimeStamp getTimestamp() {
        return clock.getTimestamp();
    }

    public void sendToLogger(TimeStampedMessage tm) {
        for (int i = 0; i < hostList.size(); i++) {
            if (hostList.get(i).name.equals("logger")) {
                hostList.get(i).sock.send(tm);

            }
        }
    }
}

class SocketHandler implements Runnable {

    public Socket sock;
    public LinkedList<MulticastMessage> receiveQueue;
    private Thread t;
    private ObjectOutputStream output = null;
    private ObjectInputStream input = null;
    ClockService clock;

    public SocketHandler(Socket newsock, ClockService newclock) {
        sock = newsock;
        clock = newclock;
        try {
            output = new ObjectOutputStream(sock.getOutputStream());
            input = new ObjectInputStream(sock.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
        receiveQueue = new LinkedList<MulticastMessage>();
    }

    public void send(TimeStampedMessage m) {
        try {
            output.writeObject(m);
        } catch (IOException ex) {
            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
    }

    public void send(MulticastMessage m) {
        try {
            output.writeObject(m);
        } catch (IOException ex) {
            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
    }

    public void run() {
        try {

            while (true) {
                MulticastMessage received = null;
                received = (MulticastMessage) input.readObject();
                receiveQueue.add(received);
            }
        } catch (IOException ex) {
            System.out.println(ex.toString());
            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE,
                    null, ex);
        } catch (ClassNotFoundException ex) {
            System.out.println(ex.toString());

            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, "a");
            t.start();
        }
    }
}

class Host implements Serializable {

    private static final long serialVersionUID = 1529127835408294641L;
    public String name;
    public SocketHandler sock;
    public String address;
    public int port;

    public Host(String newname, SocketHandler newsock, String newaddress) {
        name = newname;
        sock = newsock;
        address = newaddress;
    }

}

class Rule {

    public String action = null;
    public String src = null;
    public String dst = null;
    public String kind = null;
    public Boolean duplicate = null;
    public int seq = -1;

    public Rule(String action, String src, String dst, String kind, int seq,
            boolean duplicate) {
        this.action = action;
        this.src = src;
        this.dst = dst;
        this.kind = kind;
        this.seq = seq;
        this.duplicate = duplicate;
    }
}

abstract class ClockService {

    public abstract TimeStamp getTimestamp();

    public abstract void updateTimeStamp(TimeStamp newstamp);

    public abstract boolean happenedBefore(TimeStamp otherstamp);

    public abstract boolean happenedAfter(TimeStamp otherstamp);

    public abstract boolean concurrentWith(TimeStamp otherstamp);
}

class LogicalClock extends ClockService {

    TimeStamp stamp;

    public LogicalClock() {
        stamp = new TimeStamp("logical", 0);
    }

    @Override
    public TimeStamp getTimestamp() {
        stamp.value[0]++;
        return stamp;
    }

    @Override
    public void updateTimeStamp(TimeStamp newstamp) {
        stamp.value[0] = max(stamp.value[0], newstamp.value[0] + 1);
    }

    @Override
    public boolean happenedBefore(TimeStamp otherstamp) {
        System.out
                .println("Logical Clock cannot make \"Happened Before\" comparison");
        return false;
    }

    @Override
    public boolean concurrentWith(TimeStamp otherstamp) {
        System.out
                .println("Logical Clock cannot make \"Concurrent With\" comparison");
        return false;
    }

    @Override
    public boolean happenedAfter(TimeStamp otherstamp) {
        System.out
                .println("Logical Clock cannot make \"Happened After\" comparison");
        return false;
    }
}

class VectorClock extends ClockService {

    TimeStamp stamp;
    int size;
    int place;

    public VectorClock(int newsize, int newplace) {
        stamp = new TimeStamp("vector", newsize);
        size = newsize;
        place = newplace;
    }

    @Override
    public TimeStamp getTimestamp() {
        stamp.value[place] += 1;
        TimeStamp newtimestamp = new TimeStamp("vector", size);
        for (int i = 0; i < newtimestamp.value.length; i++) {
            newtimestamp.value[i] = stamp.value[i];
        }
        return newtimestamp;
    }

    @Override
    public void updateTimeStamp(TimeStamp newstamp) {
        String newstampS = "";
        String oldstampS = "";
        for (int i = 0; i < stamp.value.length; i++) {
            // System.out.print(newstamp.value[i]+" ");
            oldstampS += stamp.value[i] + " ";
            if (i != place) {
                stamp.value[i] = max(stamp.value[i], newstamp.value[i]);
            } else {
                stamp.value[i] = max(stamp.value[i] + 1, newstamp.value[i]);
            }
            newstampS += stamp.value[i] + " ";
        }
    }

    @Override
    public boolean happenedBefore(TimeStamp otherstamp) {
        return stamp.happenedBefore(otherstamp);
    }

    @Override
    public boolean concurrentWith(TimeStamp otherstamp) {
        return stamp.concurrentWith(otherstamp);
    }

    @Override
    public boolean happenedAfter(TimeStamp otherstamp) {
        return stamp.happenedAfter(otherstamp);
    }
}
