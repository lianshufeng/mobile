import java.io.File;

import com.bajie.project.core.util.archive.ArchiveUtil;

public class UnArchive {

	public static void main(String[] args) throws Exception {
		if (args != null && args.length > 1) {
			File inputFile = new File(args[0]);
			if (inputFile.exists()) {
				ArchiveUtil.un(inputFile, new File(args[1]));
				System.out.println("完成");
			} else {
				System.err.println("输入文件不存在");
			}
		} else {
			System.err.println("参数错误 ， 格式为:  解压文件 解压目录");
		}

	}
}
