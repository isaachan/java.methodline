package com.thoughtworks.jsa;

import java.io.File;
import java.util.List;

import com.thoughtworks.jsa.parser.MethodInfo;

public class Main {

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("Usage: java com.thoughtworks.jsa.Main [sourcefolder] [threshold] [report file path]");
			return;
		}
		
		LineOfMethodCounter counter = new LineOfMethodCounter();

		String srcFolder = args[0];
		int threshod = Integer.parseInt(args[1]);
		String reportFilePath = "method-lines-report-" + System.currentTimeMillis() + ".txt";
		if (args.length == 3) reportFilePath = args[2];
		
		List<MethodInfo> results = counter.anaylsic(new File(srcFolder));
		counter.writeReport(results, reportFilePath);
		
		MethodInfo longest = new MethodInfos(results).longest();
		System.out.println(longest);
		
		if (longest.lines >= threshod) {
			System.exit(-1);
		}
	}

}
