package com.thoughtworks.jsa;

import java.util.List;

public class MethodInfos {

	private final List<MethodInfo> infos;

	public MethodInfos(List<MethodInfo> infos) {
		this.infos = infos;
	}

	public MethodInfo longest() {
		MethodInfo theLongest = infos.get(0);
		for(MethodInfo info : infos) {
			if (theLongest.line < info.line) theLongest = info;
			
		}
		return theLongest;
	}

}
