package com.thoughtworks.jsa;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.ANTLRFileStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class LineOfMethodCounter {

	public static void main(String[] args) throws Exception {
		args = new String[] {"/home/isaac/zte/code/client/ZTE_HUB/src" };

		if (args.length != 1) {
			System.err.println("Input dir.");
			return;
		}
		
		LineOfMethodCounter counter = new LineOfMethodCounter();
		List<Info> results = new ArrayList<Info>();
		counter.analysic(new File(args[0]), results);
		
		File resultFile = new File("/home/isaac/result.txt");
//		resultFile.deleteOnExit();
//		resultFile.createNewFile();
		FileWriter writer = new FileWriter(resultFile);
		
		for(Info f : results) {
			writer.write(f.className + " " + f.methodName + ": " + f.line + "\n");
		}
		
		writer.flush();
		writer.close();
	}

	public void analysic(File file, List<Info> results) throws Exception {
		if (file.isDirectory()) {
			for(File f: file.listFiles()) {
				analysic(f, results);
			}
		} else {
			if (file.getAbsolutePath().endsWith(".java")) {
				List<Info> parseFile = parseFile(file.getAbsolutePath());
				results.addAll(parseFile);
			}
		}
	}
	
	public List<Info> parseFile(String f) throws Exception {
		JavaLexer lexer = new JavaLexer();
		lexer.setCharStream(new ANTLRFileStream(f));
		CommonTokenStream tokens = new CommonTokenStream();
		tokens.setTokenSource(lexer);
		tokens.LT(1);
		
		JavaParser parser = new JavaParser(tokens, f);
		parser.compilationUnit();
		
		return parser.lines;
	}
	
}

