#!/usr/bin/env node

module.exports = function(context) {


    //全部替换
    var replaceAll = function(text ,sourceStr , targetStr){
        return text.split(sourceStr).join(targetStr);
    }

    var fs = context.requireCordovaModule('fs'),
    path = context.requireCordovaModule('path'),
    et = context.requireCordovaModule('elementtree');
    var ConfigParser, XmlHelpers;
    try {
        // cordova-lib >= 5.3.4 doesn't contain ConfigParser and xml-helpers anymore
        ConfigParser = context.requireCordovaModule("cordova-common").ConfigParser;
        XmlHelpers = context.requireCordovaModule("cordova-common").xmlHelpers;
    } catch (e) {
        ConfigParser = context.requireCordovaModule("cordova-lib/src/configparser/ConfigParser");
        XmlHelpers = context.requireCordovaModule("cordova-lib/src/util/xml-helpers");
    }
    
    //android 目录
    var androidPlatformDir = path.join(context.opts.projectRoot,'platforms', 'android'),
    androidSrcDir = path.join(androidPlatformDir,'src'),
    projectManifestFile = path.join(androidPlatformDir,
            'AndroidManifest.xml')
    
    
    //取包名
    var projectManifestXmlRoot = XmlHelpers.parseElementtreeSync(projectManifestFile);
    manifestXmlRoot = et.parse(fs.readFileSync(projectManifestFile).toString()),
    packageName = manifestXmlRoot.getroot().get('package');
    var mainActivityPath = path.join(androidSrcDir+'/'+replaceAll(packageName,'.','/'),'MainActivity.java');
    
    //原始代码
    var codeContent = fs.readFileSync(path.join(androidSrcDir,'/com/zbj/mobile/hotupdate/HotUpdateActivity.java')).toString();
    //动态替换模版
    codeContent = replaceAll(codeContent,'package com.zbj.mobile.demo;','package '+packageName+';');
    codeContent = replaceAll(codeContent,'HotUpdateActivity','MainActivity');
    
    //保存到源码里
    fs.writeFileSync(mainActivityPath, codeContent , 'utf-8');
    
    
    console.log('Update Android Main finish.');
 

};
