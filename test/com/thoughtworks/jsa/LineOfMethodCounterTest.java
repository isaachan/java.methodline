package com.thoughtworks.jsa;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.thoughtworks.jsa.parser.MethodInfo;

public class LineOfMethodCounterTest {

	@Test
	public void sort_methodinfo_by_method_length() {
		LineOfMethodCounter counter = new LineOfMethodCounter(new Configration(new String[]{}));
		MethodInfo[] sorted = counter.sort(new ArrayList<MethodInfo>() {{
			add(createMethodInfo(5));
			add(createMethodInfo(5));
			add(createMethodInfo(10));
			add(createMethodInfo(15));
		}});
		
		assertEquals(15, sorted[0].lines);
		assertEquals(10, sorted[1].lines);
		assertEquals(5, sorted[2].lines);
		assertEquals(5, sorted[3].lines);
	}

	protected MethodInfo createMethodInfo(int line) {
		MethodInfo methodInfo = new MethodInfo("", "");
		methodInfo.lines = line;
		return methodInfo;
	}
	
}
