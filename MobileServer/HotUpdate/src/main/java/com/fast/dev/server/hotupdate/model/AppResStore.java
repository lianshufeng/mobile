package com.fast.dev.server.hotupdate.model;

/**
 * 应用资源配置
 * 
 * @作者 练书锋
 * @联系 251708339@qq.com
 * @时间 2017年8月3日
 *
 */
public class AppResStore {

	// APP的ID
	private String name;

	// Git仓库名称
	private String gitName;

	// Git 源路径, 相对路径
	private String gitSourcePath;

	// 工作空间路径，相对路径
	private String workSourcePath;

	public String getGitName() {
		return gitName;
	}

	public void setGitName(String gitName) {
		this.gitName = gitName;
	}

	public String getGitSourcePath() {
		return gitSourcePath;
	}

	public void setGitSourcePath(String gitSourcePath) {
		this.gitSourcePath = gitSourcePath;
	}

	public String getWorkSourcePath() {
		return workSourcePath;
	}

	public void setWorkSourcePath(String workSourcePath) {
		this.workSourcePath = workSourcePath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
