var exec = require('child_process').execSync;
var fs = require('fs');
var javaUtil = require('./JavaUtil');
var stringUtil = require('./StringUtil');
var scanFile = require('./ScanFileList');
var sizeOf = require('../nodejs/node_modules/image-size');
var path = require('path');

//资源列表的名称
var AssetsListName = "AssetsList.json";

//取出目标平台的路径
function getTargetPath(target, name) {
    return target + '/platforms/' + name + '/';
}

//通用更新图标
var generalIcon = function(icon, path, resInfo) {
    var info = "";
    for (key in resInfo) {
        info += resInfo[key].size + "," + resInfo[key].name + ":";
    }
    try {
        javaUtil.call("ImageConvert.groovy", [icon, path, info]);
    } catch(e) {
        console.log(e);
    }
}


//启动图片处理
var generalLaunchImage = function(config, path, resInfo){
    if (!config.app.launch ){
        console.log('跳过处理启动界面资源');
        return ;
    }
    var backgroup = config.app.launch.backgroup;
    var scale = config.app.launch.scale+'';
    var icon = config.app.launch.icon;
    if(typeof(backgroup) == 'number'){
       backgroup = '#'+backgroup.toString(16);
    }
    var info = "";
    for (key in resInfo) {
        info += resInfo[key].size + "," + resInfo[key].name + ":";
    }
    try {
        console.log('开始处理启动图片 : ' + JSON.stringify(resInfo));
        javaUtil.call("MakeLaunchImage.groovy", [backgroup , scale , icon , path, info]);
    } catch(e) {
        console.log(e);
    }


}



//处理配置文件
var navigation = function(config, content) {
    var tag = '<allow-navigation href="*" />';
    if (content.indexOf(tag) == -1) {
        var prefix = '</widget>';
        var at = content.lastIndexOf(prefix);
        var left = content.substring(0, at - 1);
        var right = content.substring(at, content.length);
        return left + '\n' + tag + '\n' + right;
    } else {
        return content;
    }
}

//处理配置文件
var versionName = function(config, content) {
    var prefix = ' version="';
    var suffix = '"';
    var oldVersion = prefix + stringUtil.subString(content, prefix, suffix) + suffix;
    var newVersion = prefix + config.app.version + suffix;
    content = content.replace(oldVersion, newVersion);
    return content;
}

//禁止拖动滑动效果
var webviewSetting = function(config, content) {
    //var tag = '<allow-navigation href="*" />';
    var tag = '<preference name="WebViewBounce" value="false" />\n<preference name="DisallowOverscroll" value="true" />';
    if (content.indexOf(tag) == -1) {
        var prefix = '</widget>';
        var at = content.lastIndexOf(prefix);
        var left = content.substring(0, at - 1);
        var right = content.substring(at, content.length);
        return left + '\n' + tag + '\n' + right;
    } else {
        return content;
    }
}

//修改配置文件
exports.updateConfig = function(config) {
    var configPath = config.output + '/config.xml'
    var buf = fs.readFileSync(configPath).toString();

    //修改允许导航的白名单
    buf = navigation(config, buf);

    //修改版本号
    buf = versionName(config, buf);
    
    //修改webview的默认设置
    buf = webviewSetting(config, buf);

    fs.writeFileSync(configPath, buf);

}

//处理资源
exports.platform = function(config) {
    console.log('处理资源');
    try {
        ios(config);
    } catch(e) {
        console.log('处理Ios资源失败.' + e);
    }
    try {
        android(config);
    } catch(e) {
        console.log('处理Android资源失败.' + e);
    }
    console.log('处理完成.');
}

//转换图像宽度与高度到字符串
var imgSizeOf = function(imagePath){
    var img = sizeOf(imagePath);
    return img.width+"_"+img.height;
}


//合并资源, 所有资源，只读资源
var mergeRes = function( allRes , readonlyRes ){
    var readWriteRes = [];
    for (var i in allRes){
        var res = allRes[i];
        if ( readonlyRes.indexOf (res) == -1 ){
            readWriteRes.push(res);
        }
    }
    var webResList = {
        'readOnlyRes':readonlyRes,
        'readWriteRes':readWriteRes
    };
    return webResList;
}

//处理android平台的资源
android = function(config) {
    var target = config.output;
    var icon = config.app.icon;
    var targetPaht = getTargetPath(target, 'android');
    if (!fs.existsSync(targetPaht)) {
        console.log('跳过Android平台的资源处理');
        return;
    }

    //Android资源处理--开始
    var images = new Array();
    var resPath = targetPaht + 'res/';
    var resList = scanFile.scan(resPath);
    for (var i in resList) {
        var iconFile = resList[i];
        if (path.basename(iconFile).toLowerCase() == 'icon.png') {
            images.push({
                "name": iconFile,
                'size': imgSizeOf(path.join(resPath ,iconFile))
            });
        }
    }
    generalIcon(icon, resPath, images);
    
  
    
    //修改启动图片
    images = new Array();
    resPath = targetPaht + 'res/';
    resList = scanFile.scan(resPath);
    for (var i in resList) {
        var file = resList[i];
        if (path.basename(file).toLowerCase() == 'screen.png') {
            images.push({
                "name": file,
                'size': imgSizeOf(path.join(resPath ,file))
            });
        }
    }
    generalLaunchImage(config, resPath, images);
    
    
    
    
    
    //icon资源处理--结束
    //生成UI资源的文件列表--开始
    var allRes = scanFile.scan(path.join( targetPaht , 'assets' , 'www'));
    var readonlyRes =  scanFile.scan(path.join( targetPaht , 'platform_www' ));
    var webResList =  mergeRes( allRes , readonlyRes );
    fs.writeFileSync(targetPaht + 'assets/' + AssetsListName, JSON.stringify(webResList));
    //生成UI资源的文件列表--结束
}

//处理IOS平台的资源
ios = function(config) {
    var target = config.output;
    var icon = config.app.icon;
    var name = config.app.name;
    var targetPaht = getTargetPath(target, 'ios');
    if (!fs.existsSync(targetPaht)) {
        console.log('跳过IOS平台的资源处理');
        return;
    }
    
    
    
    //处理Icon的图片
    var resPath = targetPaht + name + '/Images.xcassets/AppIcon.appiconset/';
    //icon资源处理--开始
    var images = new Array();
    var resList = scanFile.scan(resPath);
    for (var i in resList) {
        var iconFile = resList[i];
        if (path.extname(iconFile).toLowerCase() == '.png') {
            images.push({
                "name": iconFile,
                'size': imgSizeOf(path.join(resPath , iconFile))
            });
        }
    }
    generalIcon(icon, resPath, images);
    
    
    //处理启动画面
    //默认的图片位置
    resPath = targetPaht + name + '/Images.xcassets/LaunchImage.launchimage/';
    //icon资源处理--开始
    images = new Array();
    resList = scanFile.scan(resPath);
    for (var i in resList) {
        var file = resList[i];
        if (path.extname(file).toLowerCase() == '.png') {
            images.push({
                "name": file,
                'size': imgSizeOf(path.join(resPath , file))
            });
        }
    }
    generalLaunchImage(config, resPath, images);
    
    
    
    //icon资源处理--结束
    //生成资源的文件列表--开始
    var allRes = scanFile.scan( path.join( targetPaht , 'www' ) );
    var readonlyRes =  scanFile.scan( path.join( targetPaht , 'platform_www' ) );
    var webResList =  mergeRes( allRes , readonlyRes );  
    var AssetsFilePath = path.join(targetPaht,name,'Resources',name,'Resources',AssetsListName);
    fs.writeFileSync(AssetsFilePath, JSON.stringify(webResList));
    //生成资源的文件列表--结束
}