package com.fast.dev.hotupdate.resources.handle;

import org.apache.cordova.CordovaActivity;

import com.fast.dev.hotupdate.resources.ResourcesManager;
import com.fast.dev.hotupdate.resources.type.CallBackType;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class CallBackHandler extends Handler {

	// 实例化
	private CordovaActivity context;
	// 资源管理器
	private ResourcesManager resourcesManager;

	public CallBackHandler(CordovaActivity context, ResourcesManager resourcesManager) {
		super();
		this.context = context;
		this.resourcesManager = resourcesManager;
	}

	@Override
	public void handleMessage(Message msg) {

		Bundle bundle = msg.getData();
		String stat = bundle.getString("stat");
		if (stat.equals(CallBackType.EndUpdate.toString())) {
			int size = bundle.getInt("size");
			if (size > 0) {
				new AlertDialog.Builder(context).setTitle("更新提醒").setMessage("已更新 " + size + " 个资源，部分功能重启后生效，是否关闭程序？")
						.setPositiveButton("是", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								System.exit(0);
								//context.loadUrl(resourcesManager.getLaunchUrl());
								
							}
						}).setNegativeButton("否", null).show();
			}
		} else if (stat.equals(CallBackType.StartUpdate.toString())) {
			Toast.makeText(context, "发现新资源，正在同步。。", Toast.LENGTH_LONG).show();
		} else if (stat.equals(CallBackType.NoUpdate.toString())) {
			Log.d("ResUpdate", "无更新资源");
		} else if (stat.equals(CallBackType.CopyResFinish.toString())) {
			// 资源拷贝完后，立即进行资源检查
			resourcesManager.updateRes();
		}
	}

}
