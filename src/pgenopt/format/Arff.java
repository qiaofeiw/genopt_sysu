package pgenopt.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Arff {
	
	private final String prefix_relation = "@RELATION";
	
	private final String prefix_attribute = "@ATTRIBUTE";
	
	private final String prefix_data = "@DATA";
	
	private final String nbsp = " ";
	
	private final String br = "\n";
	
	private String Relation;
	
	private HashMap<String, String> Attribute;
	
	private String[] Data;

	public Arff(String relation, HashMap<String, String> attribute,
			String[] data) {
		super();
		Relation = relation;
		Attribute = attribute;
		Data = data;
	}
	
	public File writeToFile(String Filename) throws FileNotFoundException {
		File arff = new File(Filename);
		PrintWriter pw = new PrintWriter(arff);
		
		pw.write(prefix_relation);
		pw.write(nbsp);
		pw.write(Relation);
		pw.write(br);
		pw.write(br);
		
		for (Map.Entry<String, String> m: Attribute.entrySet()) {
			pw.write(prefix_attribute);
			pw.write(nbsp);
			pw.write(m.getKey());
			pw.write(nbsp);
			pw.write(m.getValue());
			pw.write(br);
		}
		pw.write(br);
		
		pw.write(prefix_data);
		pw.write(br);
		for (String s: Data) {
			pw.write(s);
			pw.write(br);
		}
		pw.close();
		return arff;
	}
	
}
