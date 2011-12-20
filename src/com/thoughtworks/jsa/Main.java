package com.thoughtworks.jsa;

import java.util.List;

import com.thoughtworks.jsa.parser.MethodInfo;

public class Main {

	public static void main(String[] args) throws Exception {
		Configration config = new Configration(args);
		
		if (!config.isValid()) {
			System.err.println("Usage: java com.thoughtworks.jsa.Main [-src] [sourcefolder] [-threshold] [threshold] [-ignore] [report file path]");
			return;
		}
		
		LineOfMethodCounter counter = new LineOfMethodCounter(config);
		List<MethodInfo> results = counter.anaylsic();
		counter.writeReport(results);
		
		MethodInfo longest = new MethodInfos(results).longest();
		System.out.println(longest);
		
		if (longest.lines >= config.threshold) {
			System.exit(-1);
		}
	}

}
