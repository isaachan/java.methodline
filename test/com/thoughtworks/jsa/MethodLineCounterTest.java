package com.thoughtworks.jsa;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import com.thoughtworks.jsa.parser.*;

import org.junit.Test;

public class MethodLineCounterTest {

	@Test
	public void count_line_of_instance_void_method() {
		String sourceCode = 
				"class A { \n" +
				"   public void m() {\n" +
				"       int i = 0;\n" +
				"       i++;\n" +
				"   }\n" +
				" }";
		
		InputStream input = new ByteArrayInputStream(sourceCode.getBytes());
		MethodLineCounter counter = new MethodLineCounter(input);
		
		List<MethodInfo> methodInfos = counter.getMethods();
		assertMethodInfo(methodInfos.get(0), "m", 4);
	}
	
	@Test
	public void count_line_of_instance_typed_method() {
		String sourceCode = 
				"class A { \n" +
				"   public int m() {\n" +
				"       int i = 0;\n" +
				"       return ++i;\n" +
				"   }\n" +
				" }";
		
		InputStream input = new ByteArrayInputStream(sourceCode.getBytes());
		MethodLineCounter counter = new MethodLineCounter(input);
		
		List<MethodInfo> methodInfos = counter.getMethods();
		assertMethodInfo(methodInfos.get(0), "m", 4);
	}
	
	@Test
	public void count_line_of_class_void_method() {
		String sourceCode = 
				"class A { \n" +
				"   public static void m() {\n" +
				"       int i = 0;\n" +
				"       ++i;\n" +
				"   }\n" +
				" }";
		
		InputStream input = new ByteArrayInputStream(sourceCode.getBytes());
		MethodLineCounter counter = new MethodLineCounter(input);
		
		List<MethodInfo> methodInfos = counter.getMethods();
		assertMethodInfo(methodInfos.get(0), "m", 4);
	}

	private void assertMethodInfo(MethodInfo methodInfo, String methodName, int methodLine) {
		assertEquals(methodName, methodInfo.methodName);
		assertEquals(methodLine, methodInfo.lines);
	}
	
	@Test
	public void count_line_of_class_typed_method() {
		String sourceCode = 
				"class A { \n" +
						"   public static int m() {\n" +
						"       int i = 0;\n" +
						"       return ++i;\n" +
						"   }\n" +
						" }";
		
		InputStream input = new ByteArrayInputStream(sourceCode.getBytes());
		MethodLineCounter counter = new MethodLineCounter(input);
		
		List<MethodInfo> methodInfos = counter.getMethods();
		assertMethodInfo(methodInfos.get(0), "m", 4);
	}
	
	@Test
	public void count_lines_of_method_inside_inner_class() {
		String sourceCode = 
				"class A { \n" +
				"   public void m() {\n" +
				"        Runnable r = new Runnable() {\n" +
				"            public void run() {\n" +
				"                int i = 0;\n" +
				"            }\n" +
				"        };\n" +
				"   }\n" +
				" }";
		
		InputStream input = new ByteArrayInputStream(sourceCode.getBytes());
		MethodLineCounter counter = new MethodLineCounter(input);
		
		List<MethodInfo> methodInfos = counter.getMethods();
		assertMethodInfo(methodInfos.get(0), "run", 3);
		assertMethodInfo(methodInfos.get(1), "m", 7);
	}
	
	
}
