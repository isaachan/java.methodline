package com.thoughtworks.jsa;

public class FileFilter {

	private final String[] excludes;

	public FileFilter(String[] excludes) {
		this.excludes = excludes;
	}

	public boolean filter(String f) {
		for(String exclude : excludes) {
			if (f.replace('/', '.').contains(exclude)) {
				return true;
			}
		}
		return false;
	}

}
