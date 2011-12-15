import static org.junit.Assert.*;

import org.junit.Test;

import com.thoughtworks.jsa.Info;


public class InfoTest {

	@Test
	public void compare() {
		Info info1 = createInfo(1);
		Info info2 = createInfo(3);
		
		assertEquals(0, info1.compareTo(info1));
		assertEquals(-1, info1.compareTo(info2));
		assertEquals(1, info2.compareTo(info1));
	}

	private Info createInfo(int line) {
		Info info = new Info("", "");
		info.line = line;
		return info;
	}
}
