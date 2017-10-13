package com.mobile;

import java.io.File;

import com.fast.dev.core.util.image.ImageScaleUtil;

public class ImageConvert {

	public static void main(String[] args) {
		// args = new String[] { "c:/input.png", "c:/output/image/",
		// "120,test.png:64,32@2x.png" };

		if (args.length < 2) {
			System.out.println("命令行依次为： 欲转换图片路径 目标文件夹 名称1,宽度_高度:名称2,宽度_高度");
			return;
		}
		for (String image : args[2].trim().split(":")) {
			String[] SN = image.split(",");
			if (SN.length > 1) {
				String[] whArr = SN[0].trim().split("_");
				int w = Integer.parseInt(whArr[0]);
				int h = Integer.parseInt(whArr[1]);
				String name = SN[1].trim();
				ImageScaleUtil.scale(new File(args[0]), new File(args[1] + "/" + name), w, h);
			}
		}

	}

}
