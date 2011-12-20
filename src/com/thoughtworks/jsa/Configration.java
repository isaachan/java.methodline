package com.thoughtworks.jsa;

public class Configration {

	public String sourceFolder;
	public int threshold;	
	public String[] ignorePathes = new String[] {};
	public String reportName = "method-lines-report";

	private boolean waitingForSourceFolder = false;
	private boolean waitingForThreshold = false;
	private boolean waitingForIgnore = false;
	private boolean waitingForReport;
	
	public Configration(String[] commandLineInput) {
		for (String input : commandLineInput) {
			parseCommandLine(input);
		}
	}

	private void parseCommandLine(String input) {
		if (input.equals("-src")) {
			this.waitingForSourceFolder = true;
			return;
		}
		
		if (this.waitingForSourceFolder) {
			sourceFolder = input;
			this.waitingForSourceFolder = false;
			return;
		}
		
		if (input.equals("-threshold")) {
			this.waitingForThreshold = true;
			return;
		}
		
		if (this.waitingForThreshold) {
			this.threshold = Integer.parseInt(input);
			this.waitingForThreshold = false;
			return;
		}
		
		if (input.equals("-ignore")) {
			this.waitingForIgnore = true;
			return;
		}
		
		if (this.waitingForIgnore) {
			this.ignorePathes = input.split(",");
			this.waitingForIgnore = false;
			return;
		}
		
		if (input.equals("-report")) {
			this.waitingForReport = true;
			return;
		}
		
		if (this.waitingForReport) {
			this.reportName = input;
			this.waitingForReport = false;
			return;
		}
	}

	public boolean isValid() {
		return this.threshold > 0 && (this.sourceFolder != null && this.sourceFolder.trim().length() > 0);
	}

}
