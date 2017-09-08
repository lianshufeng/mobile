package com.fast.dev.server.hotupdate.service;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 资源管理器
 * 
 * @作者: 练书锋
 * @联系: 251708339@qq.com
 */
public interface ResManagerService {

	/**
	 * 发布资源
	 * 
	 * @param resId
	 */
	public void publish(String appId, InputStream zipFileInputStream);

	/**
	 * 更新资源
	 * 
	 * @param appId
	 * @return
	 */
	public boolean update(String appId) throws Exception;

	/**
	 * 获取资源版本
	 * 
	 * @param resId
	 */
	public String getVersion(String appId);

	/**
	 * 获取资源地图的相对位置
	 * 
	 * @param resId
	 */
	public String getResMap(String appId);

	/**
	 * 取出资源文件
	 * 
	 * @param resId
	 */
	public void copyResFiles(String appId, String[] fileNames, OutputStream zipFileOutputStream);

}