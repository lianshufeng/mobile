package com.fast.dev.server.hotupdate.model;

/**
 * Git仓库配置
 * 
 * @作者 练书锋
 * @联系 251708339@qq.com
 * @时间 2017年9月11日
 *
 */
public class GitStore {

	// 名称，唯一
	private String name;

	// 本地git库的路径
	private String gitStorePath;

	// GIT 地址
	private String gitUrl;

	// GIT账号
	private String userName;

	// GIT密码
	private String passWord;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGitStorePath() {
		return gitStorePath;
	}

	public void setGitStorePath(String gitStorePath) {
		this.gitStorePath = gitStorePath;
	}

	public String getGitUrl() {
		return gitUrl;
	}

	public void setGitUrl(String gitUrl) {
		this.gitUrl = gitUrl;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

}
