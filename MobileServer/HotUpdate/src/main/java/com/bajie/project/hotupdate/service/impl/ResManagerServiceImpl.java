package com.bajie.project.hotupdate.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

import com.bajie.project.core.util.ZipUtil;
import com.bajie.project.core.util.archive.ArchiveUtil;
import com.bajie.project.core.util.code.JsonUtil;
import com.bajie.project.hotupdate.model.HotUpdateStore;
import com.bajie.project.hotupdate.service.ResManagerService;
import com.bajie.project.hotupdate.util.SVNUtil;

@Component
@SuppressWarnings("unchecked")
public class ResManagerServiceImpl implements ResManagerService {

	private static final Logger logger = LoggerFactory.getLogger(ResManagerService.class);

	@Autowired
	private ApplicationContext applicationContext;

	// 配置项
	private Map<String, HotUpdateStore> hotUpdateStoreMap = null;

	// 资源路径
	private String resDirectoryPath;
	// 配置文件后缀
	private final static String VERSUFFIX = ".ver";
	// 资源后罪名
	private final static String ASSETSSUFFIX = ".json";

	private final static SimpleDateFormat DateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

	@Autowired
	private void init(ApplicationContext applicationContext) {
		// 设置资源路径
		// resDirectoryPath = buildPlugin.getWebStaticResourcesDirectory();
		try {
			hotUpdateStoreMap = readConf();
			resDirectoryPath = this.applicationContext.getResource(".").getFile().getAbsolutePath() + "/";
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.info("WebAppStore", resDirectoryPath);
	}

	/// 将磁盘配置文件读取到内存里
	private Map<String, HotUpdateStore> readConf() throws Exception {
		Map<String, HotUpdateStore> result = new HashMap<String, HotUpdateStore>();
		Map<String, Map<String, Object>> m = JsonUtil.loadToObject("ResourcesHotUpdate.json", Map.class);
		for (Entry<String, Map<String, Object>> entry : m.entrySet()) {
			Map<String, Object> val = entry.getValue();
			HotUpdateStore hotUpdateStore = new HotUpdateStore();
			hotUpdateStore.setStorePath(String.valueOf(val.get("storePath")));
			hotUpdateStore.setSvnUrl(String.valueOf(val.get("svnUrl")));
			hotUpdateStore.setUserName(String.valueOf(val.get("userName")));
			hotUpdateStore.setPassWord(String.valueOf(val.get("passWord")));
			result.put(entry.getKey(), hotUpdateStore);
		}
		return result;
	}

	@Override
	public void publish(final String appId, final InputStream zipFileInputStream) {
		File rootPath = getResourcesRootPath(appId);

		if (!rootPath.exists()) {
			rootPath.mkdirs();
		}
		// 创建资源版本
		String newResVerion = createResVersion();

		// 解压文件
		File targetDirectory = new File(rootPath.getAbsolutePath() + "/" + appId);
		try {
			File zipFile = new File(System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString());
			FileUtils.copyInputStreamToFile(zipFileInputStream, zipFile);
			zipFileInputStream.close();
			ArchiveUtil.un(zipFile, targetDirectory);
			zipFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 刷新资源Map
		scanRes(appId, newResVerion);

		// 更新版本号
		writeResInfo(appId, VERSUFFIX, newResVerion);

	}

	@Override
	public boolean update(String appId) throws Exception {
		HotUpdateStore hotUpdateStore = hotUpdateStoreMap.get(appId);
		if (hotUpdateStore == null) {
			return false;
		}

		// 创建资源版本
		String newResVerion = createResVersion();

		// svn客户端
		final SVNClientManager svnClientManager = SVNUtil.authSvn(hotUpdateStore.getSvnUrl(),
				hotUpdateStore.getUserName(), hotUpdateStore.getPassWord());
		// SVN的资源Store
		File svnPath = new File(getResourcesRootPath(appId).getAbsolutePath() + "/" + appId);
		if (svnPath.exists()) {
			// 更新最新版本，且无穷的深度
			SVNUtil.update(svnClientManager, svnPath, SVNRevision.HEAD, SVNDepth.INFINITY);
		} else {
			SVNURL svnurl = SVNURL.parseURIEncoded(hotUpdateStore.getSvnUrl());
			SVNUtil.checkout(svnClientManager, svnurl, SVNRevision.HEAD, svnPath, SVNDepth.INFINITY);
		}
		// 销毁
		svnClientManager.dispose();

		// 刷新资源Map
		scanRes(appId, newResVerion);

		// 更新版本号
		writeResInfo(appId, VERSUFFIX, newResVerion);

		return true;
	}

	@Override
	public String getVersion(String appId) {
		return readResInfo(appId, VERSUFFIX);
	}

	@Override
	public String getResMap(String appId) {
		HotUpdateStore hotUpdateStore = this.hotUpdateStoreMap.get(appId);
		if (hotUpdateStore != null) {
			return hotUpdateStore.getStorePath() + "/" + appId + ASSETSSUFFIX;
		}
		return null;
	}

	@Override
	public void copyResFiles(String appId, String[] files, OutputStream zipFileOutputStream) {
		File rootPath = getResourcesRootPath(appId);
		// 获取绝对路径的地址
		Map<String, File> fileMap = new HashMap<String, File>();
		if (files != null) {
			for (String fileName : files) {
				File f = new File(rootPath.getAbsolutePath() + "/" + appId + "/" + fileName);
				if (f.exists()) {
					fileMap.put(fileName, f);
				}
			}
		}

		ZipUtil.zip(zipFileOutputStream, fileMap);

	}

	/**
	 * 扫描资源目录下的所有文件,并将资源添加置内存里
	 * 
	 * @param resId
	 */
	private synchronized void scanRes(String resId, String version) {
		File rootPath = getResourcesRootPath(resId);
		if (rootPath.isDirectory()) {
			List<File> list = new ArrayList<File>();
			File source = new File(rootPath.getAbsolutePath() + "/" + resId);
			scanFiles(source, list);
			// 载入资源hash
			Map<String, String> fileMap = new ConcurrentHashMap<String, String>();
			for (File f : list) {
				try {
					CRC32 crc32 = new CRC32();
					crc32.update(FileUtils.readFileToByteArray(f));
					fileMap.put(relativePath(source, f), Long.toHexString(crc32.getValue()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			Map<String, Object> m = new HashMap<>();
			m.put("version", version);
			m.put("map", fileMap);
			// 保存到磁盘上
			try {
				writeResInfo(resId, ASSETSSUFFIX, JsonUtil.toJson(m));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 扫描
	 * 
	 * @param file
	 * @param list
	 */
	private static void scanFiles(final File file, final List<File> list) {
		for (File f : file.listFiles()) {
			if (f.isDirectory()) {
				if (!f.getName().equalsIgnoreCase(".svn")) {
					scanFiles(f, list);
				}
			} else {
				list.add(f);
			}
		}
	}

	/**
	 * 取出相对路径
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	private static String relativePath(final File source, final File target) {
		return ZipUtil.relativeName(source.getAbsolutePath(), target.getAbsolutePath());
	}

	/***
	 * 创建资源版本号
	 * 
	 * @return
	 */
	private static String createResVersion() {
		// 使用时间作为版本号
		return DateFormat.format(new Date(System.currentTimeMillis()));
	}

	/**
	 * 从磁盘上保存资源版本
	 */
	private void writeResInfo(String resId, String suffixName, String content) {
		File rootPath = getResourcesRootPath(resId);
		// 写版本号
		File verFile = new File(rootPath.getAbsolutePath() + "/" + resId + suffixName);
		try {
			FileUtils.writeStringToFile(verFile, content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从磁盘上读取资源版本
	 * 
	 * @param resId
	 * @return
	 */
	private String readResInfo(String resId, String suffixName) {
		File rootPath = getResourcesRootPath(resId);
		if (rootPath == null) {
			return null;
		}
		// 写版本号
		File verFile = new File(rootPath.getAbsolutePath() + "/" + resId + suffixName);
		try {
			return verFile.exists() ? FileUtils.readFileToString(verFile) : null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// 获取根目录资源
	private File getResourcesRootPath(String resId) {
		HotUpdateStore hotUpdateStore = this.hotUpdateStoreMap.get(resId);
		if (hotUpdateStore == null) {
			return null;
		}
		return new File(this.resDirectoryPath + hotUpdateStore.getStorePath());
	}

}
