package com.thoughtworks.jsa;

import static org.junit.Assert.*;

import org.junit.Test;

public class FileFilterTest {

	@Test
	public void should_ignore_excludes_file() {
		FileFilter filter = new FileFilter(new String[] {"org.jivesoftware"});
		assertTrue(filter.filter("/home/isaac/zte/code/client/ZTE_HUB/src/org/jivesoftware/smack/proxy/Socks5ProxySocketFactory.java"));
		assertFalse(filter.filter("/home/isaac/zte/code/client/ZTE_HUB/src/com/ztehub/activities/ZteMainActivity.java"));
	}
	
	@Test
	public void should_ignore_excludes_files() {
		FileFilter filter = new FileFilter(new String[] {"org.jivesoftware", "org.apache"});
		
		assertTrue(filter.filter("/home/isaac/zte/code/client/ZTE_HUB/src/org/jivesoftware/smack/proxy/Socks5ProxySocketFactory.java"));
		assertTrue(filter.filter("/home/isaac/zte/code/client/ZTE_HUB/src/org/apache/harmony/javax/security/auth/login/Account.java"));
		
		assertFalse(filter.filter("/home/isaac/zte/code/client/ZTE_HUB/src/com/ztehub/activities/ZteMainActivity.java"));
	}
}
