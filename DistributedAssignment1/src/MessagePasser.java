import java.awt.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;


public class MessagePasser {
    //done
	
	public static ArrayList<Host> hostList = new ArrayList<Host>();
	public static int seqNum;
	
	public MessagePasser(String pathName, String localName){
		seqNum = 0;

		Map<String, Object> data = getYamlData(pathName);

		// for host in configFile
		//new Socket host
		Host h = new Host(null, null, null);
		hostList.add(h);

	}
	
	public Map<String, Object> getYamlData(String pathName){
		InputStream input = null;
		try {
			input = new FileInputStream(new File(
			        pathName));
		} catch (FileNotFoundException e) {
			System.out.println("Could not find configuration file");
			e.printStackTrace();
		}
		Yaml yaml = new Yaml();
		Map<String, Object> configData = (Map<String, Object>) yaml.load(input);
		return configData;
	}
	
}