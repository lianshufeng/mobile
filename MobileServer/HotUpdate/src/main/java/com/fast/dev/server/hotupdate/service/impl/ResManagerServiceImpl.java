package com.fast.dev.server.hotupdate.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.fast.dev.component.archive.ArchiveUtil;
import com.fast.dev.core.util.ZipUtil;
import com.fast.dev.core.util.code.JsonUtil;
import com.fast.dev.server.hotupdate.model.AppResStore;
import com.fast.dev.server.hotupdate.model.GitStore;
import com.fast.dev.server.hotupdate.service.ResManagerService;
import com.fast.dev.server.hotupdate.util.GitUtil;

@Component
@SuppressWarnings("unchecked")
public class ResManagerServiceImpl implements ResManagerService {

	private static final Logger logger = LoggerFactory.getLogger(ResManagerService.class);

	@Autowired
	private ApplicationContext applicationContext;

	// 配置项
	private Map<String, AppResStore> appResStoreMap = null;

	// git仓库
	private Map<String, GitStore> gitStoreMap = null;

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
			appResStoreMap = readConf("AppResStores.json", AppResStore.class);
			gitStoreMap = readConf("GitStores.json", GitStore.class);
			resDirectoryPath = this.applicationContext.getResource(".").getFile().getAbsolutePath() + "/";
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.info("WebAppStore", resDirectoryPath);
	}

	private <T> T readConf(String configName, Class<?> cls) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> lists = JsonUtil.loadToObject(configName, List.class);
		for (Map<String, Object> val : lists) {
			Object obj = cls.newInstance();
			BeanUtils.populate(obj, val);
			result.put(String.valueOf(val.get("name")), obj);
		}
		return (T) result;
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
	public boolean update(final String appId) throws Exception {
		// 应用资源
		AppResStore appResStore = this.appResStoreMap.get(appId);
		if (appResStore == null) {
			return false;
		}
		// 仓库资源
		String gitName = appResStore.getGitName();
		if (gitName == null) {
			return false;
		}
		GitStore gitStore = this.gitStoreMap.get(gitName);
		if (gitStore == null) {
			return false;
		}
		// 创建资源版本
		String newResVerion = createResVersion();
		// 更新git并同步到工作空间
		updateGitToWork(appResStore, gitStore);
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
		AppResStore appResStore = this.appResStoreMap.get(appId);
		if (appResStore != null) {
			return appResStore.getWorkSourcePath() + "/" + appId + ASSETSSUFFIX;
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
			Map<String, Long> fileMap = new ConcurrentHashMap<String, Long>();
			for (File f : list) {
				try {
					CRC32 crc32 = new CRC32();
					crc32.update(FileUtils.readFileToByteArray(f));
					fileMap.put(relativePath(source, f), crc32.getValue());
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
				if (!f.getName().equalsIgnoreCase(".svn") && !f.getName().equalsIgnoreCase(".git")) {
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
		AppResStore appResStore = this.appResStoreMap.get(resId);
		if (appResStore == null) {
			return null;
		}
		return new File(this.resDirectoryPath + appResStore.getWorkSourcePath());
	}

	/**
	 * 更新资源并同步到工作空间
	 * 
	 * @param workFile
	 * @param gitStore
	 * @throws IOException
	 */
	private void updateGitToWork(AppResStore appResStore, GitStore gitStore) throws IOException {
		// 应用ID
		String appId = appResStore.getName();
		// 工作空间
		File workFile = new File(getResourcesRootPath(appId).getAbsolutePath() + "/" + appId);
		// git仓库目录
		File gitStoreFile = new File(gitStore.getGitStorePath());
		// 拉取或者更新代码
		GitUtil.pull(gitStore.getGitUrl(), gitStore.getUserName(), gitStore.getPassWord(), gitStoreFile);
		// 删除工作空间
		if (workFile.exists()) {
			FileUtils.cleanDirectory(workFile);
		} else {
			workFile.mkdirs();
		}

		// 拷贝文件
		File source = new File(gitStoreFile.getAbsolutePath() + "/" + appResStore.getGitSourcePath());
		if (source.exists()) {
			FileUtils.copyDirectory(source, workFile, new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return !pathname.getName().equalsIgnoreCase(".git");
				}
			});
		}

	}

}
