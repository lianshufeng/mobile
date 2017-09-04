package com.zbj.mobile.hotupdate.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * ZIP 压缩工具
 * 
 * @作者: 练书锋
 * @联系: 251708339@qq.com
 */
public class ZipUtil {

	/**
	 * 压缩文件
	 * 
	 * @param file
	 * @param m
	 * @throws IOException
	 */
	public static void zip(final OutputStream outputStream, final Map<String, File> m) {
		ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
		for (Entry<String, File> e : m.entrySet()) {
			try {
				if (e.getValue().exists()) {
					// 数据
					ZipEntry zipEntry = new ZipEntry(e.getKey());
					zipOutputStream.putNextEntry(zipEntry);

					FileInputStream fileInputStream = new FileInputStream((e.getValue()));
					StreamUtils.copy(fileInputStream, zipOutputStream);
					fileInputStream.close();
					zipOutputStream.closeEntry();
				}

			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		try {
			zipOutputStream.finish();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	/**
	 * 压缩资源文件 注：压缩管道结束后，必须执行 finish
	 * 
	 * @param outputStream
	 * @param file
	 * @throws IOException
	 */
	public static void zip(final ZipOutputStream zipOutputStream, final String fileName, final InputStream inputStream)
			throws IOException {
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOutputStream.putNextEntry(zipEntry);
		StreamUtils.copy(inputStream, zipOutputStream);
	}

	/**
	 * 压缩目录
	 * 
	 * @param outputStream
	 * @param directory
	 */
	public static void zipDirectory(final OutputStream outputStream, final File directory) {
		if (directory.exists()) {
			Map<String, File> m = new HashMap<String, File>();
			listFile(directory.getAbsolutePath(), directory, m);
			zip(outputStream, m);
		}
	}

	/**
	 * 解压文件
	 * 
	 * @param sourceZip
	 * @param targetDirectory
	 * @throws IOException
	 */
	public static List<String> unZipFile(final File sourceZip, final File targetDirectory) throws IOException {
		List<String> list = new ArrayList<String>();
		com.file.zip.ZipFile zipFile = new com.file.zip.ZipFile(sourceZip, "GBK");
		Enumeration<? extends com.file.zip.ZipEntry> entries = zipFile.getEntries();
		while (entries.hasMoreElements()) {
			com.file.zip.ZipEntry entry = entries.nextElement();
			File entryDestination = new File(targetDirectory, entry.getName());
			if (entry.isDirectory()) {
				entryDestination.mkdirs();
			} else {
				InputStream in = zipFile.getInputStream(entry);
				File pathFile = new File(FilenameUtils.getPath(entryDestination.getAbsolutePath()));
				if (!pathFile.exists()) {
					pathFile.mkdirs();
				}
				OutputStream out = new FileOutputStream(entryDestination);
				IOUtils.copy(in, out);
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
				list.add(entry.getName());
			}
		}

		return list;

	}

	/**
	 * 解压文件
	 * 
	 * @param inputStream
	 * @param targetDirectory
	 * @throws IOException
	 */
	public static List<String> unZipFile(final InputStream inputStream, final File targetDirectory) throws IOException {
		List<String> list = new ArrayList<String>();
		// 创建或清空目录
		if (!targetDirectory.exists()) {
			targetDirectory.mkdirs();
		}
		ZipInputStream zipInputStream = new ZipInputStream(inputStream);
		ZipEntry zipEntry;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			try {
				final File file = new File(targetDirectory.getAbsolutePath() + "/" + zipEntry.getName());
				if (zipEntry.isDirectory()) {
					file.mkdirs();
				} else {
					File pFile = new File(file.getParent());
					if (!pFile.exists()) {
						pFile.mkdirs();
					}
					FileOutputStream fileOutputStream = new FileOutputStream(file);
					StreamUtils.copy(zipInputStream, fileOutputStream);
					fileOutputStream.close();
					list.add(zipEntry.getName());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			zipEntry.clone();
			zipInputStream.closeEntry();
		}
		return list;
	}

	/**
	 * 深度遍历取出文件路径
	 * 
	 * @param file
	 * @param list
	 */
	private static void listFile(final String rootDirectory, final File directory, final Map<String, File> m) {
		if (directory.isDirectory()) {
			for (File f : directory.listFiles()) {
				if (f.isDirectory()) {
					listFile(rootDirectory, f, m);
				} else {
					String fileName = relativeName(rootDirectory, f.getAbsolutePath());
					m.put(fileName, f);
				}
			}
		} else {
			String fileName = relativeName(rootDirectory, directory.getAbsolutePath());
			m.put(fileName, directory);
		}
	}

	/**
	 * 取出相对路径
	 * 
	 * @param rootDirectory
	 * @param directory
	 * @return
	 */
	private static String relativeName(final String rootDirectory, final String directory) {
		// 取出相对路径
		String path = FileUtil.relative(rootDirectory, directory);
		// 保证第一个出现的字符不能为/
		if (path.length() > 0) {
			if (path.substring(0, 1).equals("/")) {
				path = path.substring(1, path.length());
			}
		}
		return path;
	}

}
