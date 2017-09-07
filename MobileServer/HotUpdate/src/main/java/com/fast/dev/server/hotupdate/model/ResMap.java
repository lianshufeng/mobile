package com.fast.dev.server.hotupdate.model;

import java.util.Map;

//资源对象
public class ResMap {

	private String version;

	private Map<String, String> map;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}

}
