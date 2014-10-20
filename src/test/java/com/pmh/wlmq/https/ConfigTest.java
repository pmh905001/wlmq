package com.pmh.wlmq.https;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConfigTest {

	@Test
	public void test() {
		
		String loadDate = Config.getInstance().getLoadDate();
//		assertEquals("2014-10-31", loadDate);
	}

}
