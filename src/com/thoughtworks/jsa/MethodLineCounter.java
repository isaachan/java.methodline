package com.thoughtworks.jsa;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import com.thoughtworks.jsa.parser.JavaLexer;
import com.thoughtworks.jsa.parser.JavaParser;
import com.thoughtworks.jsa.parser.MethodInfo;

public class MethodLineCounter {

	private final InputStream input;

	public MethodLineCounter(InputStream input) {
		this.input = input;
	}

	public List<MethodInfo> getMethods() {
		try {
			JavaLexer lexer = new JavaLexer(new ANTLRInputStream(input));
			CommonTokenStream tokens = new CommonTokenStream();
			tokens.setTokenSource(lexer);
			tokens.LT(1);
			JavaParser parser = new JavaParser(tokens);
			parser.compilationUnit();
			return parser.lines;
		} catch (Exception e) {
			throw new RuntimeException("Java file is invalid.");
		}
	}

}
