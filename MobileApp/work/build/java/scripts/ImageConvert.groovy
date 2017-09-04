package com.mobile;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageConvert {
	public static boolean narrowAndFormateTransfer(String srcPath,
			String destPath, int height, int width, String format) {
		boolean flag = false;
		try {
			File file = new File(srcPath);
			File destFile = new File(destPath);
			if (!destFile.getParentFile().exists()) {
				destFile.getParentFile().mkdirs();
			}
			BufferedImage src = ImageIO.read(file); // 读入文件
			Image image = src.getScaledInstance(width, height,
					Image.SCALE_DEFAULT);
			BufferedImage tag = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics g = tag.getGraphics();
			g.drawImage(image, 0, 0, null); // 绘制缩小后的图
			g.dispose();
			flag = ImageIO.write(tag, format, new FileOutputStream(destFile));// 输出到文件流
		} catch (IOException e) {
			e.printStackTrace();
		}
		return flag;
	}

	public static String main(String[] args) {
		// args = new String[] { "c:/input.png", "c:/output/image/",
		// "120,test.png:64,32@2x.png" };

		if (args.length < 2) {
			System.out.println("命令行依次为： 欲转换图片路径 目标文件夹 名称1,宽度_高度:名称2,宽度_高度");
			return;
		}
		for (String image : args[2].trim().split(":")) {
			String[] SN = image.split(",");
			if (SN.length > 1) {
				String [] whArr = SN[0].trim().split("_");
				int w = Integer.parseInt(whArr[0]);
				int h = Integer.parseInt(whArr[1]);
				String name = SN[1].trim();
				narrowAndFormateTransfer(args[0], args[1] + "/" + name, h,
						w, "png");
			}
		}

		// narrowAndFormateTransfer(args[0], args[1], Integer.parseInt(args[2]),
		// Integer.parseInt(args[3]), args[4]);
		//System.out.println("finish");
		return "finish"
	}

}
