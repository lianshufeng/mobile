package com.fast.dev.hotupdate;

import org.apache.cordova.CordovaActivity;

import com.fast.dev.hotupdate.resources.ResourcesManager;
import com.fast.dev.hotupdate.resources.impl.ResourcesManagerImpl;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

@SuppressLint("HandlerLeak")
public class HotUpdateActivity extends CordovaActivity {

	private ResourcesManager resManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		resManager = new ResourcesManagerImpl(this);
		loadUrl(resManager.getLaunchUrl());
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.resManager.updateRes();
	}
	
	
	@Override
	public void onReceivedError(int errorCode, String description, String failingUrl) {
		// TODO Auto-generated method stub
//		super.onReceivedError(errorCode, description, failingUrl);
		Log.e("onReceivedError:", description);
		Log.e("failingUrl:", failingUrl);
	}

}
