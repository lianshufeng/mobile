package com.zbj.mobile.hotupdate.resources;

/**
 * 资源管理器
 * 
 * @作者 练书锋
 * @时间 2015年1月9日
 *
 */
public abstract class ResourcesManager {

	/**
	 * 开始更新资源
	 */
	public abstract void updateRes();

	/**
	 * 获取启动的url
	 * 
	 * @return
	 */
	public abstract String getLaunchUrl();
}
