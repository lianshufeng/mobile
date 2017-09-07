package com.fast.dev.server.hotupdate.util;

import java.io.File;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class GitUtil {

	/**
	 * 拉取GIT仓库
	 * 
	 * @param uri
	 * @param userName
	 * @param passWord
	 * @param workPath
	 */
	public static void pull(final String uri, final String userName, final String passWord, final File workPath) {
		Git gitResult = null;
		try {
			// 权限认证
			final UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
					userName, passWord.toCharArray());
			// 判断是否已经被加载
			if (new File(workPath.getAbsolutePath() + "/.git").exists()) {
				gitResult = Git.open(workPath);
			} else {
				CloneCommand cloneCommand = Git.cloneRepository();
				cloneCommand.setURI(uri);
				cloneCommand.setDirectory(workPath);
				cloneCommand.setCredentialsProvider(credentialsProvider);
				gitResult = cloneCommand.call();
			}
			gitResult.pull().setCredentialsProvider(credentialsProvider).call();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (gitResult != null) {
				gitResult.close();
			}
		}

	}

}
