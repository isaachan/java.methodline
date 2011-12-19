package com.thoughtworks.jsa;

import java.util.List;

import com.thoughtworks.jsa.parser.MethodInfo;

public class MethodInfos {

	private final List<MethodInfo> infos;

	public MethodInfos(List<MethodInfo> infos) {
		this.infos = infos;
	}

	public MethodInfo longest() {
		MethodInfo theLongest = infos.get(0);
		for(MethodInfo info : infos) {
			if (theLongest.lines < info.lines) theLongest = info;
			
		}
		return theLongest;
	}

}
