import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable {
	private static final long serialVersionUID = 1529127835408294641L;
	public String name;
	public ArrayList<String> members;
	public Group(String name, ArrayList members) {
		this.name = name;
		this.members = members;
	}

}
