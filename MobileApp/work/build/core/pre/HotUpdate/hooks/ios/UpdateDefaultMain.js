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
    
    //ios 目录
    var iosPlatformDir = path.join(context.opts.projectRoot,'platforms', 'ios'),
    configFilePath = path.join(context.opts.projectRoot,'config.xml');
    configFXmlRoot = et.parse(fs.readFileSync(configFilePath).toString()),
    projectName = configFXmlRoot.findtext('./name'),
    iosSrcDir = path.join(iosPlatformDir,projectName,'Classes'),
    mainViewController_h_Path = path.join(iosSrcDir,'MainViewController.h'),
    mainViewController_m_Path = path.join(iosSrcDir,'MainViewController.m');
    
    //修改程序入口
    var headTempl = '#import "UpdateRes.h"\n',
    headSource = fs.readFileSync(mainViewController_h_Path).toString();
    //判断是否已经修改过
    if (headSource.indexOf(headTempl) == -1){
        var offSet = headSource.indexOf('@interface'),
        headLeft = headSource.substring(0,offSet),
        headright = headSource.substring(offSet,headSource.length),
        newHSource = headLeft +headTempl+headright;
        fs.writeFileSync(mainViewController_h_Path, newHSource , 'utf-8');
    }
    
    
    //
    var contentAtStr = '- (void)viewDidLoad';
    var contentPartFilePath = path.join(path.dirname(context.scriptLocation),'..','..','src','ios','MainViewController.part'),
    partContent = fs.readFileSync(contentPartFilePath).toString(),
    contentSource = fs.readFileSync(mainViewController_m_Path).toString();
    //判断是否已经修改过
    if (contentSource.indexOf(partContent)==-1){
        var offSetStart = contentSource.indexOf( contentAtStr ),
        offSetEnd = contentSource.indexOf( '}' , offSetStart ),
        contentLeft = contentSource.substring( 0 , offSetStart ),
        contentRight = contentSource.substring( offSetEnd + 1, contentSource.length ),
        content = contentLeft + partContent + contentRight;
        fs.writeFileSync(mainViewController_m_Path, content , 'utf-8');
    }
   
    
    
    
    
    /**
     var mainActivityPath = path.join(androidSrcDir+'/'+replaceAll(packageName,'.','/'),'MainActivity.java');
    //原始代码
    var codeContent = fs.readFileSync(path.join(androidSrcDir,'/com/zbj/mobile/hotupdate/HotUpdateActivity.java')).toString();
    //动态替换模版
    codeContent = replaceAll(codeContent,'package com.zbj.mobile.demo;','package '+packageName+';');
    codeContent = replaceAll(codeContent,'HotUpdateActivity','MainActivity');
    
    //保存到源码里
    fs.writeFileSync(mainActivityPath, codeContent , 'utf-8');
    **/

    
    console.log('Update IOS Main finish.');
 

};
