package com.mobile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fast.dev.core.util.image.ImageMergeUtil;

public class MakeLaunchImage {

	public static void main(String[] args) {
		// args = new String[] { "#EEEE00", "0.618" ,"c:/output/image/",
		// "120,test.png:64,32@2x.png" };

		if (args.length < 3) {
			System.out.println("����������Ϊ�� ����ͼ ������ Ŀ���ļ��� ����1,���_�߶�:����2,���_�߶�");
			return;
		}

		String backgroup = args[0];
		double scale = Double.parseDouble(args[1]);
		String icon = args[2];
		String path = args[3];
		String info = args[4];
		int rgb = -1;

		// �ж��Ƿ�ʹ��rgb
		if (backgroup.substring(0,1).equals("#")) {
			rgb = Integer.parseInt(backgroup.substring(1, backgroup.length()), 16);
		}

		for (String image : info.trim().split(":")) {
			String[] SN = image.split(",");
			if (SN.length > 1) {
				String[] whArr = SN[0].trim().split("_");
				int w = Integer.parseInt(whArr[0]);
				int h = Integer.parseInt(whArr[1]);
				String name = SN[1].trim();
				File sourceImageFile = new File(icon);
				File outputFile = new File(path + "/" + name);
				
				if (rgb == -1) {
					ImageMergeUtil.mergeToCenter(sourceImageFile, scale, new File(backgroup), outputFile, w, h);
				} else {
					ImageMergeUtil.mergeToCenter(sourceImageFile, scale, rgb, outputFile, w, h);
				}
			}
		}

	}

}
