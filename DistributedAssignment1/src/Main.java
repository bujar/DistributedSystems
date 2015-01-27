public class Main {
	public static void main(String[] args) {
		
		//will change to retrieving paramaters from user args
		MessagePasser msg = new MessagePasser("configuration.yml", "bob");
                msg.send(new Message("alice","MX","bla"));
                System.out.println(msg.receive());
	}
}
