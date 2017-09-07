package com.bajie.project.hotupdate.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.bajie.project.core.environments.ENVFactory;
import com.bajie.project.core.environments.ENVModel;
import com.bajie.project.core.model.ExceptionModel;
import com.bajie.project.core.model.InvokerResult;
import com.bajie.project.core.util.ResponseUtil;
import com.bajie.project.core.util.net.UrlUtil;
import com.bajie.project.hotupdate.service.ResManagerService;

@RequestMapping("HotUpdate")
public class ResController {

	@Autowired
	private ResManagerService resManagerService;

	/**
	 * 提交zip压缩资源的文件
	 * 
	 * @param appId
	 * @param resFile
	 * @return
	 */
//	@RequestMapping("publish.json")
	public InvokerResult<Object> publish(String appId, MultipartFile resFile) {
		try {
			this.resManagerService.publish(appId, resFile.getInputStream());
			return new InvokerResult<Object>("操作完成！");
		} catch (Exception e) {
			e.printStackTrace();
			return new InvokerResult<Object>(new ExceptionModel(String.valueOf(e.getClass()), e.getMessage()));
		}
	}

	@RequestMapping("update.json")
	public InvokerResult<Object> update(String appId) throws Exception {
		Object o = this.resManagerService.update(appId);
		return new InvokerResult<Object>(o);
	}

	@RequestMapping("getVersion")
	public void getVersion(HttpServletRequest request, HttpServletResponse response, String appId) throws Exception {
		Map<String, Object> m = new HashMap<>();
		m.put("time", System.currentTimeMillis());
		m.put("version", this.resManagerService.getVersion(appId));
		m.put("appId", appId);
		ResponseUtil.write(request, response, m);
	}

	@RequestMapping("getMap")
	public void getMap(HttpServletRequest request, HttpServletResponse response, String appId) throws IOException {
		ENVModel envModel = ENVFactory.create(request);
		String version = this.resManagerService.getVersion(appId);
		String assetMapUrl = envModel.getRoot() + resManagerService.getResMap(appId) + "?" + version;
		response.sendRedirect(UrlUtil.format(assetMapUrl));
	}

	@RequestMapping("getRes")
	public void getRes(HttpServletResponse httpServletResponse, String appId, String[] fileNames) {
		httpServletResponse.setHeader("content-disposition", "attachment;filename=" + appId + ".zip");
		try {
			OutputStream outputStream = httpServletResponse.getOutputStream();
			this.resManagerService.copyResFiles(appId, fileNames, outputStream);
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
