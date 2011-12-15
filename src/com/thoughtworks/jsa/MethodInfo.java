package com.thoughtworks.jsa;

public class MethodInfo implements Comparable<MethodInfo> {
	public MethodInfo(String className, String methodName) {
		this.className = className;
		this.methodName = methodName;
	}
	public String className;
	public String methodName;
	public int line;
	
	@Override
	public String toString() {
		return className + " " + methodName + ": " + line;
	}
	
	@Override
	public int compareTo(MethodInfo another) {
		if (this.line == another.line)
			return 0;
		if (this.line > another.line) 
			return -1;
		return 1;
	}
}