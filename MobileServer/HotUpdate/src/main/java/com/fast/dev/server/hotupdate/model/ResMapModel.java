package com.fast.dev.server.hotupdate.model;

import java.util.Map;

/**
 * 资源模型
 * 
 * @作者: 练书锋
 * @联系: 251708339@qq.com
 */
public class ResMapModel {
	// 文件资源列表
	private Map<String, String> fileMap;
	// 版本号
	private String version;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Map<String, String> getFileMap() {
		return fileMap;
	}

	public void setFileMap(Map<String, String> fileMap) {
		this.fileMap = fileMap;
	}

}
