var fs = require('fs'),
    os = require('os'),
    path = require('path'),
    exec = require('child_process').execSync,
    uuid = require('../nodejs/node_modules/node-uuid'),
    folder = require('./folder.js'),
    cordova = require('./Cordova'),
    commandUtil = require('./CommandUtil'),
    javaUtil = require('./JavaUtil');


var templatePath = process.cwd() + '/build/nodejs/node_modules/cordova-app-hello-world/template_src';

var importCordova = function(projectPath) {
    var indexPath = projectPath + '/index.html';
    if (fs.existsSync(indexPath)) {
        var indexHtml = fs.readFileSync(indexPath).toString();
        var hAt = indexHtml.toLowerCase().indexOf('<head>');
        var left = indexHtml.substring(0, hAt + 6);
        var right = indexHtml.substring(hAt + 6, indexHtml.length - hAt - 6);
        fs.writeFileSync(indexPath, left + '<script type="text/javascript" src="cordova.js"></script>' + right);
    }
}

//读取 mobileprovision 文件的UUID
var readProvisionFileUUID = function(filePath) {
    var content = fs.readFileSync(filePath).toString();
    var startAt = content.indexOf('<key>UUID</key>');
    var endAt = content.indexOf('</string>', startAt);
    content = content.substring(startAt, endAt);
    content = content.substring(content.lastIndexOf('>') + 1, content.length);
    return content;
}

//拷贝文件
var copyFile = function(source, target) {
    fs.writeFileSync(target, fs.readFileSync(source));
}

//创建项目
exports.projects = function(config) {
    console.log('创建项目');
    //创建项目目录
    folder.mkdirs(config.output);
    if (!fs.existsSync(config.output)) {
        fs.mkdirSync(config.output);
    }
    //项目名
    var baseName = path.basename(config.output);
    //复制项目配置工程
    var projectConfig = path.join(path.dirname(config.output), baseName + '_install','project');
    folder.copy(templatePath, projectConfig);
    //如果资源文件存在，则解压资源文件
    if (fs.existsSync(config.project)) {
        var cmds = [];
        commandUtil.append(cmds, config.project, true);
        commandUtil.append(cmds, path.join(projectConfig , 'www'), true);
        var ret = javaUtil.call('UnArchive.groovy', cmds);
        console.log('准备项目资源完成该.');
    }
    //创建项目
    cordova.create(config.output, config.app.package, config.app.name, projectConfig);
    console.log('创建完成.');
}

//添加平台
exports.add = function(config) {
    console.log('添加平台');
    var platPath = process.cwd() + '/build/nodejs/node_modules/';
    try {
        console.log('添加Android平台');
        cordova.addPlatform(config.output, platPath + 'cordova-android');
    } catch(e) {
        console.log('添加Android平台失败 : '+e);
    }
    try {
        console.log('添加IOS平台');
        cordova.addPlatform(config.output, platPath + 'cordova-ios');
    } catch(e) {
        console.log('添加IOS平台失败 : '+ e);
    }
    console.log('添加完成.');
}

var buildAndroid = function(config) {
    console.log('开始编译 Android');
    var androidCmd = ['android', '--', '--release', '--keystore="' + config.sign.android.keystore + '"', '--storePassword="' + config.sign.android.storePassword + '"', '--alias="' + config.sign.android.alias + '"', '--password="' + config.sign.android.password + '"']
    var cmd = commandUtil.toCommandLine(androidCmd);
    cordova.build(config.output, cmd);
    console.log('编译完成 .');
}

//编译IOS
var buildIos = function(config) {
    console.log('开始编译 IOS');
    var signRootPath = os.homedir() + '/Library/MobileDevice/Provisioning Profiles/';
    //开发证书
    var developmentFileName = readProvisionFileUUID(config.sign.ios.developmentProfile),
    developmentFilePath = path.join(signRootPath, developmentFileName + '.mobileprovision');
    //发布证书
    var distributionFileName = readProvisionFileUUID(config.sign.ios.distributionProfile),
    distributionFilePath = path.join(signRootPath, distributionFileName + '.mobileprovision');
    //拷贝证书到签名目录
    copyFile(config.sign.ios.developmentProfile, developmentFilePath);
    copyFile(config.sign.ios.distributionProfile, distributionFilePath);
    var iosCmd = ['ios', '--device', '--provisioningProfile="' + developmentFileName + '"', '--developmentTeam="' + config.sign.ios.developmentTeam + '"', '--packageType="' + config.sign.ios.packageType + '"']
    var cmd = commandUtil.toCommandLine(iosCmd);
    try {
        cordova.build(config.output, cmd);
    } catch(e) {
        console.log('编译IOS失败:' + e);
    }
    //删除签名后的证书
    fs.unlinkSync(developmentFilePath);
    fs.unlinkSync(distributionFilePath);
    console.log('编译完成 .');
}

//编译项目
exports.build = function(config) {
    try {
        buildAndroid(config);
    } catch(e) {
        console.log('编译 Android 失败:' + e);
    }

    try {
        buildIos(config);
    } catch(e) {
        console.log('编译 IOS 失败:' + e);
    }

}