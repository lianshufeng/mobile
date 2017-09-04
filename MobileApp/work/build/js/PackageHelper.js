var exec = require('child_process').execSync;
var fs = require('fs');
var path = require('path');
var folder = require('./Folder');
var urlUtil = require('./UrlUtil');

//取出目标平台的路径
function getTargetPath(target, name) {
    return path.join(target , 'platforms' , name);
}

//拷贝文件
function copyFile(source, target) {
    fs.writeFileSync(target, fs.readFileSync(source));
}

var iosSignApp = function(config) {
    var targetPath = getBuildPath(config);
    var signPath = config.output + '/tmp/iReSign.app';
    var sourceSignPath = process.cwd() + '/build/libs/iReSign/bin/iReSign.app'
    //复制到临时目录里
    folder.copy(sourceSignPath, signPath);
    //赋予权限
    exec('chmod -R 777 ' + signPath);
    //修改配置文件
    copyFile(sourceSignPath + '.plist', signPath + '.plist');
    var tmp = fs.readFileSync(signPath + '.plist');
    tmp = tmp.toString();
    tmp = tmp.replace('[cert]', config.sign.ios.cert);
    tmp = tmp.replace('[provision]', config.sign.ios.provision);
    tmp = tmp.replace('[ipa]', targetPath + config.app.name + '.ipa');
    fs.writeFileSync(signPath + '.plist', tmp);
    //执行签名
    exec('open -W ' + signPath);
}

var makeIpaIcon = function(config) {

    var icon = config.app.icon;
    var buildPath = getBuildPath(config);
    var command = 'java -jar ' + process.cwd() + '/build/libs/image/ConvertImage.jar ';
    command += '"' + icon + '" ';
    command += '"' + buildPath + '" ';
    command += '64,' + path.basename(config.server.icon);
    try {
        exec(command);
    } catch(e) {
        console.log(e);
    }

}

//制作plist文件: 提供下载功能
var makePlistFile = function(config) {
    var buildPath = getBuildPath(config);
    var ipaPlist = process.cwd() + '/templates/plist/ipa.plist';
    var buf = fs.readFileSync(ipaPlist).toString();
    buf = buf.replace('${title}', config.app.name);
    buf = buf.replace('${version}', config.app.version);
    buf = buf.replace('${package}', config.app.package);
    buf = buf.replace('${display-image}', urlUtil.format(config.server.url + '/' + config.server.icon));
    buf = buf.replace('${software-package}', urlUtil.format(config.server.url + '/' + config.server.ipa));
    fs.writeFileSync(buildPath + config.app.name + '.plist', buf);
}

//打包app的路径
var getBuildPath = function(config) {
    var buildPath = path.join(config.output,'build');
    if (!fs.existsSync(buildPath)) {
        folder.mkdirs(buildPath);
    }
    return buildPath;
}

//打包android应用
var androidPackage = function(config) {
    var platformsPath = getTargetPath(config.output, 'android');
    if (!fs.existsSync(platformsPath)) {
        console.log('跳过Android平台打包.');
        return;
    }
    var targetPath = getBuildPath(config);
    var buildPath = path.join(platformsPath,'build','outputs','apk');
    //按照顺序优先拷贝签名过的
    var apkFileName = ['android-armv7-debug.apk', 'android-armv7-release.apk', 'android-debug.apk', 'android-release.apk']
    for (var i in apkFileName) {
        var apkFile = path.join(buildPath , apkFileName[i]);
        if (fs.existsSync(apkFile)) {
            copyFile(apkFile, path.join(targetPath , config.app.name + '.apk'));
            break;
        }
    }
}

//打包IOS应用
var iosPackage = function(config) {
    var targetPath = getBuildPath(config);
    var appFileName = config.app.name+'.ipa';
    var platformsPath = getTargetPath(config.output, 'ios');
    var buildPath = path.join(platformsPath ,'build','device',appFileName)
    if (!fs.existsSync(buildPath)) {
        console.log('跳过IOS平台打包');
        return;
    }
     copyFile(buildPath, path.join(targetPath , appFileName));
}

exports.export = function(config) {
    //资源打包
    console.log('开始打包应用.');
    try {
        androidPackage(config);
    } catch(e) {
        console.log('打包android应用失败:'+e);
    }
    try {
        iosPackage(config);
    } catch(e) {
        console.log('打包IOS应用失败:'+e);
    }
    console.log('打包完成:' + getBuildPath(config));
}