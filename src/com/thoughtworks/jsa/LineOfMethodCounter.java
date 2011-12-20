package com.thoughtworks.jsa;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.ANTLRFileStream;

import com.thoughtworks.jsa.parser.JavaLexer;
import com.thoughtworks.jsa.parser.JavaParser;
import com.thoughtworks.jsa.parser.MethodInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LineOfMethodCounter {

	private String[] ignorePathes; 
//	= new String[] {
//			"org.apache",
//			"org.de",
//			"org.jivesoftware", 
//			"org.kenai",
//			"org.novell",
//			"org.xbill",
//			
//			"java.net.sf.kraken",
//			"java.com.jivesoftware"
//		};
	private final Configration config;

	public LineOfMethodCounter(Configration config) {
		this.config = config;
		this.ignorePathes = config.ignorePathes;
	}

	public void writeReport(List<MethodInfo> results) throws IOException {
		File resultFile = new File(this.config.reportName);
		resultFile.delete();
		resultFile.createNewFile();
		FileWriter writer = new FileWriter(resultFile);
		
		MethodInfo[] sorted = this.sort(results);

		for(MethodInfo f : sorted) {
			writer.write(f.className + " " + f.methodName + ": " + f.lines + "\n");
		}
		
		writer.flush();
		writer.close();
	}

	public List<MethodInfo> anaylsic() throws Exception {
		File file = new File(config.sourceFolder);
		ArrayList<MethodInfo> results = new ArrayList<MethodInfo>();
		analysic(file, results);
		return results;
	}
	
	public void analysic(File file, List<MethodInfo> results) throws Exception {
		if (file.isDirectory()) {
			for(File f: file.listFiles()) {
				analysic(f, results);
			}
		} else {
			if (isValidFile(file)) {
				List<MethodInfo> parseFile = parseFile(file.getAbsolutePath());
				results.addAll(parseFile);
			}
		}
	}

	private boolean isValidFile(File file) {
		String filePath = file.getAbsolutePath();
		return filePath.endsWith(".java") && !new FileFilter(this.ignorePathes).filter(filePath);
	}
	
	public List<MethodInfo> parseFile(String f) throws Exception {
		JavaLexer lexer = new JavaLexer();
		
		lexer.setCharStream(new ANTLRFileStream(f, "utf-8"));
		CommonTokenStream tokens = new CommonTokenStream();
		tokens.setTokenSource(lexer);
		tokens.LT(1);
		
		JavaParser parser = new JavaParser(tokens, f);
		parser.compilationUnit();
		
		return parser.lines;
	}

	public MethodInfo[] sort(List<MethodInfo> arrayList) {
		MethodInfo[] infos = new MethodInfo[arrayList.size()];
		arrayList.toArray(infos);
		Arrays.sort(infos);
		return infos;
	}
	
}








