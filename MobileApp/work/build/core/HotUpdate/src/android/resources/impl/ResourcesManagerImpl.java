package com.fast.dev.hotupdate.resources.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.zip.CRC32;

import org.apache.commons.io.FileUtils;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import com.fast.dev.hotupdate.resources.ResourcesManager;
import com.fast.dev.hotupdate.resources.handle.CallBackHandler;
import com.fast.dev.hotupdate.resources.model.UpdateListModel;
import com.fast.dev.hotupdate.resources.type.CallBackType;
import com.fast.dev.hotupdate.util.FileUtil;
import com.fast.dev.hotupdate.util.HttpClient;
import com.fast.dev.hotupdate.util.HttpClient.ResultBean;
import com.fast.dev.hotupdate.util.StreamUtils;
import com.fast.dev.hotupdate.util.ZipUtil;
import com.google.common.io.ByteStreams;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ResourcesManagerImpl extends ResourcesManager {

	// 资源配置URL
	private static final String VersionUrl = "ActionVersion";
	// 地图资源流
	private static final String MapUrl = "ActionMap";
	// 获取压缩的资源列表
	private static final String ResUrl = "ActionResources";
	// 版本名称
	private final static String FilesVersionName = "files.ver";

	// 拷贝线程的资源
	private Thread runCopyResThread = null;
	// 检查是否在
	private boolean isRunCheckResThread = false;
	// 上下文
	private Activity context;
	// 回调方法
	private Handler callBackHandler;

	public ResourcesManagerImpl(CordovaActivity context) {
		super();
		this.context = context;
		callBackHandler = new CallBackHandler(context, this);

	}

	/**
	 * 获取资源main页
	 * 
	 * @return
	 */
	public synchronized String getLaunchUrl() {
		return copyResFiles();
	}

	/**
	 * 开始检查更新任务 启动新线程
	 */
	public synchronized void updateRes() {
		// 资源路径
		File resourcesFile = getResourcesFile();
		final File buildFile = new File(resourcesFile.getAbsolutePath() + "/build");
		// 里面存放版本号，如果存在，则说明是扩展数据
		if (buildFile.exists()) {
			if (isRunCheckResThread) {
				Log.i("debug", "检查缓线程已启动，操作被取消！");
				return;
			}
			Log.i("debug", "启动线程，开始检查资源！");
			isRunCheckResThread = true;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						startUpdateTask();
					} catch (Exception e) {
						e.printStackTrace();
					}
					isRunCheckResThread = false;
				}
			}).start();
		}
	}

	/**
	 * 创建通信url
	 * 
	 * @param actionName
	 * @return
	 */
	private String createActionUrl(String actionName) {
		String actionUri = getServerAction(actionName);
		String spaceName = actionUri.indexOf("?") == -1 ? "?" : "&";
		actionUri = actionUri.replaceAll("\\\\", "/");
		actionUri = actionUri.replaceAll("//", "/");
		if (actionUri.substring(0, 1).equals("/")) {
			actionUri = actionUri.substring(1, actionUri.length());
		}
		String url = getServerUrl() + actionUri + spaceName + "appId=" + getAppId();
		return url;
	}

	/**
	 * 判断是否有新版
	 */
	private boolean checkNewVersion() {
		return !loadLocalVersion().equals(loadServerVersion());
	}

	/**
	 * 载入本地的版本号
	 * 
	 * @return
	 */
	private String loadLocalVersion() {
		File resourcesFile = getResourcesFile();
		File resConf = new File(resourcesFile.getAbsolutePath() + "/" + FilesVersionName);
		byte[] bin = null;
		if (resConf.exists()) {
			try {
				bin = FileUtils.readFileToByteArray(resConf);
				return new String(bin, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	/**
	 * 保存到本地版本
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private void saveLocalVersion(final String version) throws UnsupportedEncodingException, IOException {
		File resourcesFile = getResourcesFile();
		File resConf = new File(resourcesFile.getAbsolutePath() + "/" + FilesVersionName);
		FileUtils.writeByteArrayToFile(resConf, version.getBytes("UTF-8"));
	}

	/**
	 * 载入服务器版本
	 * 
	 * @return
	 */
	private String loadServerVersion() {
		HttpClient httpClient = new HttpClient();
		try {
			byte[] bin = httpClient.ReadDocuments(createActionUrl(VersionUrl)).getData();
			String result = new String(bin, "UTF-8");
			try {
				JSONObject jsonObject = new JSONObject(result);
				String content = jsonObject.getString("version");
				return content == null ? "" : content;
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// 拷贝JSON数组资源到缓存
	private void copyJsonResToCache(String resTypeName, List<String> files, List<String> updateFiles)
			throws JSONException {
		List<String> resArr = readAppResList(resTypeName);
		for (int i = 0; i < resArr.size(); i++) {
			String resName = FileUtil.format(String.valueOf(resArr.get(i)));
			files.add(FileUtil.format("www" + "/" + resName));
			updateFiles.add(resName.substring(0, 1).equals("/") ? resName.substring(1, resName.length()) : resName);
		}
	}

	/**
	 * 拷贝资源
	 * 
	 * @return
	 */
	private String copyResFiles() {
		final File resourcesFile = getResourcesFile();

		// 可读写资源的根目录
		final File targetPath = new File(resourcesFile.getAbsolutePath() + "/www");
		if (!targetPath.exists()) {
			targetPath.mkdirs();
		}
		// 里面存放版本号
		final File buildFile = new File(resourcesFile.getAbsolutePath() + "/build");

		// 取出当前APP的版本号
		final String versionName = getVersionName();
		boolean needCopy = false;
		if (!buildFile.exists()) {
			needCopy = true;
		} else {
			try {
				needCopy = !new String(FileUtils.readFileToByteArray(buildFile)).equals(versionName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 复制资源
		if (needCopy && runCopyResThread == null) {
			runCopyResThread = new Thread(new Runnable() {
				@Override
				public void run() {
					final List<String> files = new ArrayList<String>();
					final List<String> updateFiles = new ArrayList<String>();
					try {
						// 拷贝只读资源
						copyJsonResToCache("readOnlyRes", files, updateFiles);
						// 拷贝读写资源
						copyJsonResToCache("readWriteRes", files, updateFiles);
					} catch (Exception e) {
						e.printStackTrace();
					}
					// // 解压文件
					for (String resName : files) {
						try {
							// 资源原路径
							InputStream resInputStream = context.getAssets().open(resName);
							// 资源目标路径
							File targetFile = new File(resourcesFile.getAbsolutePath() + "/" + resName);
							if (!targetFile.getParentFile().exists()) {
								targetFile.getParentFile().mkdirs();
							}
							FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
							// 拷贝管道流
							ByteStreams.copy(resInputStream, fileOutputStream);
							fileOutputStream.close();
							resInputStream.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					try {
						// 更新缓存资源hash
						updateResCache(updateFiles);
						// 写app版本号到文件，表示安装成功
						FileUtils.writeByteArrayToFile(buildFile, versionName.getBytes());
						// 清空本地资源号，因为可能更新非首次安装
						saveLocalVersion("");
						// 拷贝资源完成
						sendHandlerMessage(CallBackType.CopyResFinish.toString(), new Bundle());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			runCopyResThread.start();
		}
		String mainPath = "file://" + (needCopy ? "/android_asset/www/" : (targetPath.getAbsolutePath() + "/"))
				+ getConfigMainHtml();
		Log.d("载入主页", mainPath);
		return mainPath;
	}

	// 线程中的
	private void startUpdateTask() throws Exception {
		Bundle data = new Bundle();
		boolean isNeedUpdate = checkNewVersion();
		if (isNeedUpdate) {
			UpdateListModel updateListModel = getUpdateResList();
			List<String> files = updateListModel.getUpdates();
			data.putInt("size", files.size());
			if (files.size() > 0) {
				sendHandlerMessage(CallBackType.StartUpdate.toString(), data);
				Log.d("更新的文件：", String.valueOf(files));
				updateRes(files);
				data.putInt("size", files.size());
			} else {
				Log.d("无更新资源", "");
			}
			// 删除过期资源
			removeFileList(updateListModel.getDelList());

			// 更新本地的hash缓存
			updateResCache(updateListModel.getAllList());

			// 保存版本信息到本地
			saveLocalVersion(updateListModel.getVersion());
			sendHandlerMessage(CallBackType.EndUpdate.toString(), data);
		} else {
			Log.d("无更新的资源版本", "");
			sendHandlerMessage(CallBackType.NoUpdate.toString(), data);
		}

	}

	// 删除过期资源
	private void removeFileList(List<String> delList) {
		// 取出资源目录
		final File resourcesFile = getResourcesFile();
		// 可读写资源的根目录
		final File targetPath = new File(resourcesFile.getAbsolutePath() + "/www");
		// 删除资源文件
		for (String fileName : delList) {
			new File(targetPath.getAbsolutePath() + "/" + fileName).delete();
		}
	}

	// 发送消息给回调
	private void sendHandlerMessage(String stat, Bundle bundle) {
		if (callBackHandler != null) {
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putAll(bundle);
			data.putString("stat", stat);
			msg.setData(data);
			callBackHandler.sendMessage(msg);
		}
	}

	/**
	 * 更新资源
	 * 
	 * @param files
	 * @throws Exception
	 */
	private void updateRes(final List<String> files) throws Exception {
		File wwwResources = new File(getResourcesFile().getAbsolutePath() + "/www");
		if (!wwwResources.exists()) {
			wwwResources.mkdirs();
		}
		// 将要处理的资源容器
		List<String> res = new ArrayList<String>();
		for (int i = 0; i < files.size(); i++) {
			// 增加资源到将要处理的列表里
			res.add(files.get(i));
			if (i % 20 == 0) {
				downloadAndUpdate(res, wwwResources);
			}
		}
		downloadAndUpdate(res, wwwResources);
	}

	/**
	 * 下载资源并更新
	 * 
	 * @param files
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private void downloadAndUpdate(final List<String> res, final File wwwResources)
			throws UnsupportedEncodingException, IOException {
		// 没有资源则不处理下载更新列表
		if (res.size() == 0) {
			return;
		}
		Log.d("更新文件：", String.valueOf(res));
		HttpClient httpClient = new HttpClient();
		String postInfo = "";
		for (String name : res) {
			postInfo += "&fileNames=" + name;
		}
		byte[] bin = httpClient.ReadDocuments(createActionUrl(ResUrl), true, postInfo.getBytes("UTF-8")).getData();
		File tmpFile = new File(System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString() + ".zip");
		FileUtils.writeByteArrayToFile(tmpFile, bin);
		ZipUtil.unZipFile(tmpFile, wwwResources);
		tmpFile.delete();
		res.clear();
	}

	/**
	 * 读取app的资源
	 * 
	 * @param typeName
	 * @return
	 */
	private List<String> readAppResList(String typeName) {
		List<String> res = null;
		try {
			res = new ArrayList<String>();
			byte[] bin = StreamUtils.copyToByteArray(context.getAssets().open("AssetsList.json"));
			JSONObject jsonObject = new JSONObject(new String(bin));
			// 拷贝只读资源
			JSONArray readOnlyArr = jsonObject.getJSONArray(typeName);
			for (int i = 0; i < readOnlyArr.length(); i++) {
				res.add(readOnlyArr.getString(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;

	}

	/**
	 * 开始更新
	 *
	 * @throws IOException
	 * @throws JSONException
	 */
	private UpdateListModel getUpdateResList() throws IOException, JSONException {
		UpdateListModel updateListModel = new UpdateListModel();
		List<String> needUpdateList = new ArrayList<String>();
		List<String> allList = new ArrayList<String>();
		HttpClient httpClient = new HttpClient();
		ResultBean resultBean = httpClient.ReadDocuments(createActionUrl(MapUrl));
		JSONObject content = new JSONObject(new String(resultBean.getData(), resultBean.getCharset()));
		if (content != null) {
			// 设置版本号
			updateListModel.setVersion(content.getString("version"));
			content = content.getJSONObject("map");
			final File resourcesFile = getResourcesFile();
			final String targetPath = resourcesFile.getAbsolutePath() + "/www/";
			Iterator<?> iterator = content.keys();
			final JSONObject filehash = readResCacheHash();
			while (iterator.hasNext()) {
				// 文件名
				String key = String.valueOf(iterator.next());
				// 服务端的文件hash
				String remoteHash = content.getString(key);
				// 本地缓存的文件hash， 每次都删除一个，如果没有被删除则说明该文件是本地多余的文件
				String localHash = String.valueOf(filehash.remove(key));
				// 所有的资源名称
				allList.add(key);
				try {
					// 判断文件是否存在，保证一定会得到更新
					File file = new File(targetPath + key);
					if (file.exists()) {
						// 避免字符串比较错误
						if (Long.parseLong(localHash, 16) != Long.parseLong(remoteHash, 16)) {
							needUpdateList.add(key);
						}
					} else {
						needUpdateList.add(key);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// 获取只读文件列表
			List<String> readOnlyRes = readAppResList("readOnlyRes");
			// 计算得出没有被使用过的文件
			Iterator<String> delList = filehash.keys();
			List<String> dels = new ArrayList<String>();
			while (delList.hasNext()) {
				String delFile = delList.next();
				delFile = delFile.substring(0, 1).equals("/") ? delFile : "/" + delFile;
				if (!readOnlyRes.contains(delFile)) {
					dels.add(delFile);
				}
			}
			updateListModel.setDelList(dels);
		}

		// 需要更新的列表
		updateListModel.setUpdates(needUpdateList);
		// 所有的资源
		updateListModel.setAllList(allList);
		return updateListModel;
	}

	// 取出Meta数据
	private Bundle getMetaData() {
		ApplicationInfo info = null;
		try {
			info = this.context.getPackageManager().getApplicationInfo(this.context.getPackageName(),
					PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return info.metaData;
	}

	// 取出服务端地址
	private String getServerUrl() {
		String url = getBundle().getString("ServerUrl");
		if (!url.substring(url.length() - 1, url.length()).equals("/")) {
			url += "/";
		}
		return url;
	}

	// 取AppId
	private String getAppId() {
		return getBundle().getString("MobileAppid");
	}

	/**
	 * 取出服务端的action
	 * 
	 * @param actionName
	 * @return
	 */
	private String getServerAction(String actionName) {
		return getBundle().getString(actionName);
	}

	/**
	 * 取Bundle
	 * 
	 * @return
	 */
	private Bundle getBundle() {
		return getMetaData();
	}

	private String getVersionName() {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	// 获取配置文件配置的初始页面
	private String getConfigMainHtml() {
		int id = this.context.getResources().getIdentifier("config", "xml",
				this.context.getClass().getPackage().getName());
		if (id == 0) {
			// If we couldn't find config.xml there, we'll look in the namespace
			// from AndroidManifest.xml
			id = this.context.getResources().getIdentifier("config", "xml", this.context.getPackageName());
			if (id == 0) {
				LOG.e("config", "res/xml/config.xml is missing!");
				return null;
			}
		}
		XmlResourceParser xml = this.context.getResources().getXml(id);
		int eventType = -1;
		String srcUrl = "index.html";
		while (eventType != XmlResourceParser.END_DOCUMENT) {
			if (eventType == XmlResourceParser.START_TAG) {
				String strNode = xml.getName();
				if (strNode.equals("content")) {
					String src = xml.getAttributeValue(null, "src");
					if (src != null) {
						srcUrl = src;
					}
				}
			}
			try {
				eventType = xml.next();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return srcUrl;
	}

	// 取出另存为的资源路径
	private File getResourcesFile() {
		final File resourcesFile = new File(context.getFilesDir().getAbsolutePath() + "/resources");
		if (!resourcesFile.exists()) {
			resourcesFile.mkdirs();
		}
		return resourcesFile;
	}

	/**
	 * 更新文件缓存hash
	 * 
	 * @throws IOException
	 * @throws JSONException
	 */
	private synchronized void updateResCache(final List<String> list) throws IOException, JSONException {
		// 取出资源目录
		final File resourcesFile = getResourcesFile();
		// 可读写资源的根目录
		final File targetPath = new File(resourcesFile.getAbsolutePath() + "/www");
		// 读取以前的资源
		// JSONObject jsonObject = readResCacheHash();
		JSONObject jsonObject = new JSONObject();
		// 计算更新的文件hash
		for (String resName : list) {
			File file = new File(targetPath.getAbsolutePath() + "/" + resName);
			CRC32 crc32 = new CRC32();
			crc32.update(FileUtils.readFileToByteArray(file));
			String fileHash = Long.toHexString(crc32.getValue());
			jsonObject.put(resName, fileHash);
		}
		// 保存hash列表到缓存
		FileUtils.writeStringToFile(new File(resourcesFile.getAbsolutePath() + "/files.hash"), jsonObject.toString());
		System.out.println("更新文件缓存hash完成. size:" + list.size());
	}

	/**
	 * 读取文件hash缓存
	 * 
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	private synchronized JSONObject readResCacheHash() throws IOException, JSONException {
		JSONObject jsonObject = null;
		// 取出资源目录
		final File resourcesFile = getResourcesFile();
		final File file = new File(resourcesFile.getAbsolutePath() + "/files.hash");
		if (file.exists()) {
			// 读取缓存hash列表转
			byte[] bin = FileUtils.readFileToByteArray(file);
			jsonObject = new JSONObject(new String(bin));
		} else {
			jsonObject = new JSONObject();
		}
		return jsonObject;

	}

}
