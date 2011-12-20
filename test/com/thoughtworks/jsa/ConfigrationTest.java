package com.thoughtworks.jsa;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConfigrationTest {

	@Test
	public void get_src_folder_from_commandline_input() {
		Configration config = new Configration(new String[] {"-src", "/home/isaac/code"});
		assertEquals("/home/isaac/code", config.sourceFolder);
	}
	
	@Test
	public void get_threshold_from_commandline_input() {
		Configration config = new Configration(new String[] {"-threshold", "100"});
		assertEquals(100, config.threshold);
	}

	@Test
	public void get_ignore_pathes_from_commandline_input() {
		Configration config = new Configration(new String[] {"-ignore", "/home/isaac/code,/home/isaac/opensource"});
		assertEquals(2, config.ignorePathes.length);
		assertEquals("/home/isaac/code", config.ignorePathes[0]);
		assertEquals("/home/isaac/opensource", config.ignorePathes[1]);
	}
	
	@Test
	public void report_has_a_default_name() {
		Configration config = new Configration(new String[] {});
		assertEquals("method-lines-report", config.reportName);
	}
	
	@Test
	public void get_report_name_from_commandline_input() {
		Configration config = new Configration(new String[] {"-report", "reportName"});
		assertEquals("reportName", config.reportName);
	}
	
	@Test
	public void get_all_from_commandline_input() {
		Configration config = new Configration(new String[] {"-src", "/home/isaac/code", "-threshold", "100", "-ignore", "/home/isaac"});
		assertEquals("/home/isaac/code", config.sourceFolder);
		assertEquals(100, config.threshold);
		assertEquals("/home/isaac", config.ignorePathes[0]);
		assertTrue(config.isValid());
	}
	
	@Test
	public void check_configration() {
		assertTrue(new Configration(new String[] {"-src", "/home/isaac/code", "-threshold", "100"}).isValid());
		assertFalse(new Configration(new String[] {"-src", "/home/isaac/code"}).isValid());
		assertFalse(new Configration(new String[] {"-threshold", "100"}).isValid());
	}
	
	@Test
	public void default_ignore_pathes_is_empty() {
		assertEquals(0, new Configration(new String[] {}).ignorePathes.length);
	}
}
