package com.thoughtworks.jsa;

public class Info implements Comparable<Info> {
	public Info(String className, String methodName) {
		this.className = className;
		this.methodName = methodName;
	}
	public String className;
	public String methodName;
	public int line;
	
	@Override
	public int compareTo(Info another) {
		if (this.line == another.line)
			return 0;
		if (this.line > another.line) 
			return 1;
		return -1;
	}
}