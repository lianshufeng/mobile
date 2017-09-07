package com.fast.dev.server.hotupdate.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fast.dev.core.environments.ENVFactory;
import com.fast.dev.core.environments.ENVModel;
import com.fast.dev.core.model.InvokerResult;
import com.fast.dev.core.util.ResponseUtil;
import com.fast.dev.core.util.net.UrlUtil;
import com.fast.dev.server.hotupdate.service.ResManagerService;

@RequestMapping("HotUpdate")
public class ResController {

	@Autowired
	private ResManagerService resManagerService;

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
