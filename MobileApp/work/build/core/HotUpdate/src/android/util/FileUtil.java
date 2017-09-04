package com.zbj.mobile.hotupdate.util;

import java.io.File;
import java.util.List;

public class FileUtil {
	/**
	 * 寻找指定目录下的资源文件
	 * 
	 * @param file
	 * @param files
	 */
	public static void listFiles(final File targetFile, List<File> files) {
		if (targetFile == null || files == null) {
			return;
		}
		for (File file : targetFile.listFiles()) {
			if (file.isDirectory()) {
				listFiles(file, files);
			} else {
				files.add(file);
			}
		}
	}

	/**
	 * 格式化文件
	 * 
	 * @param file
	 * @return
	 */
	public static String format(final File file) {
		return file.getAbsolutePath();
	}

	/**
	 * 格式化文件
	 * 
	 * @param file
	 * @return
	 */
	public static String format(final String file) {
		String path = file;
		while (path.indexOf("\\") > -1) {
			path = path.replaceAll("\\\\", "/");
		}
		while (path.indexOf("//") > -1) {
			path = path.replaceAll("//", "/");
		}
		return path;
	}

	/**
	 * 取出相当路径
	 * 
	 * @param rootPath
	 *            根目录
	 * @param path
	 * @return
	 */
	public static String relative(final String rootPath, final String path) {
		String formatRootPath = format(rootPath);
		String formatPath = format(path);
		String left = formatPath.substring(0, formatRootPath.length());
		if (left.equals(formatRootPath)) {
			return formatPath.substring(formatRootPath.length(),
					formatPath.length());
		}
		return null;
	}

	/**
	 * 取出相对路径
	 * 
	 * @param rootPath
	 * @param path
	 * @return
	 */
	public static String relative(final File rootPath, final File path) {
		String formatRootPath = format(rootPath);
		String formatPath = format(path);
		return relative(formatRootPath, formatPath);
	}

}
