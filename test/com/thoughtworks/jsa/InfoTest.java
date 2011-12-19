package com.thoughtworks.jsa;
import static org.junit.Assert.*;

import org.junit.Test;

import com.thoughtworks.jsa.parser.MethodInfo;


public class InfoTest {

	@Test
	public void compare() {
		MethodInfo info1 = createInfo(1);
		MethodInfo info2 = createInfo(3);
		
		assertEquals(0, info1.compareTo(info1));
		assertEquals(-1, info1.compareTo(info2));
		assertEquals(1, info2.compareTo(info1));
	}

	private MethodInfo createInfo(int line) {
		MethodInfo info = new MethodInfo("", "");
		info.lines = line;
		return info;
	}
}
