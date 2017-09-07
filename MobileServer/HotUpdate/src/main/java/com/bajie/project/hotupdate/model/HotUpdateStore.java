package com.bajie.project.hotupdate.model;

/**
 * 热更新配置
 * 
 * @作者 练书锋
 * @联系 251708339@qq.com
 * @时间 2017年8月3日
 *
 */
public class HotUpdateStore {

	// APP的ID
	private String storePath;

	// SVN 地址
	private String svnUrl;

	// SVN账号
	private String userName;

	// Svn密码
	private String passWord;

	public String getStorePath() {
		return storePath;
	}

	public void setStorePath(String storePath) {
		this.storePath = storePath;
	}

	public String getSvnUrl() {
		return svnUrl;
	}

	public void setSvnUrl(String svnUrl) {
		this.svnUrl = svnUrl;
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
