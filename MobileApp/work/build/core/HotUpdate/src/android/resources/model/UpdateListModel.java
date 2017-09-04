package com.zbj.mobile.hotupdate.resources.model;

import java.util.List;

//资源对象
public class UpdateListModel {

	// 版本号
	private String version;

	// 需要更新的列表
	private List<String> updates;

	// 需要删除的列表
	private List<String> delList;

	// 远程文件列表
	private List<String> allList;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<String> getUpdates() {
		return updates;
	}

	public void setUpdates(List<String> updates) {
		this.updates = updates;
	}

	public List<String> getDelList() {
		return delList;
	}

	public void setDelList(List<String> delList) {
		this.delList = delList;
	}

	public List<String> getAllList() {
		return allList;
	}

	public void setAllList(List<String> allList) {
		this.allList = allList;
	}

}
