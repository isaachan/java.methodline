package com.thoughtworks.jsa.parser;

public class MethodInfo implements Comparable<MethodInfo> {
	
	public String className;
	public String methodName;
	public int lines;
	
	public MethodInfo(String className, String methodName) {
		this.className = className;
		this.methodName = methodName;
	}

	@Override
	public String toString() {
		return className + " " + methodName + ": " + lines;
	}
	
	@Override
	public int compareTo(MethodInfo another) {
		if (this.lines == another.lines)
			return 0;
		if (this.lines > another.lines) 
			return -1;
		return 1;
	}
}