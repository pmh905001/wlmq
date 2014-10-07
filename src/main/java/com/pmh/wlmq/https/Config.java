/*
 * Copyright (c) 2014. General Electric Company. All rights reserved.
 *  The copyright to the computer software herein is the property of
 *  General Electric Company. The software may be used and/or copied only
 *  with the written permission of General Electric Company or in accordance
 *  with the terms and conditions stipulated in the agreement/contract
 *  under which the software has been supplied.
 */

package com.pmh.wlmq.https;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

public class Config {
	private static Config instance = null;

	private Properties prop = new Properties();

	private Config() {
	}

	public static Config getInstance() {
		if (instance == null) {
			instance = new Config();
			instance.loadProperties();
		}
		return instance;
	}

	void loadProperties() {
		String propertiesFile = "config.properties";
		try {
			InputStream resourceAsStream = this.getClass().getClassLoader()
					.getResourceAsStream(propertiesFile);
			prop.load(new InputStreamReader(resourceAsStream,"UTF-8"));
		} catch (IOException e) {
			URL resource = this.getClass().getClassLoader()
					.getResource(propertiesFile);
			String path = (resource == null) ? null : resource.getPath();
			throw new RuntimeException(String.format("load %s(%s) fail",
					propertiesFile, path), e);
		}
	}

	public String getProperty(String key, String defaultValue) {
		return this.prop.getProperty(key, defaultValue);
	}
	
	public String getProperty(String key) {
		return this.prop.getProperty(key);
	}
}
