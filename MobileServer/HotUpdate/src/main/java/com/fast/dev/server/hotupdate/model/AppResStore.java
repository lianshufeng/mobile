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

	private String appId;

	// APP的ID
	private String storePath;

	// GIT 地址
	private String gitUrl;

	// GIT账号
	private String userName;

	// GIT密码
	private String passWord;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getStorePath() {
		return storePath;
	}

	public void setStorePath(String storePath) {
		this.storePath = storePath;
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
