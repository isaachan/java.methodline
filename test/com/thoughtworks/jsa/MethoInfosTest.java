package com.thoughtworks.jsa;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.thoughtworks.jsa.parser.MethodInfo;

public class MethoInfosTest {

	@Test
	public void should_find_longest_method() {
		List<MethodInfo> infos = new ArrayList<MethodInfo>() {{
			add(createInfo(5));
			add(createInfo(6));
		}};
		
		MethodInfo info = new MethodInfos(infos).longest();
		assertEquals(6, info.lines);
	}
	
	private MethodInfo createInfo(int line) {
		MethodInfo info = new MethodInfo("", "");
		info.lines = line;
		return info;
	}
}
