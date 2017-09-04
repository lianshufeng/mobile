var fs = require('fs'),
path = require('path'),
folder = require('./folder.js'),
cordova = require('./cordova'),
javaUtil = require('./JavaUtil'),
commandUtil = require('./CommandUtil'),
modulePath = process.cwd()+'/build/nodejs/node_modules/';


//替换配置信息
replaceConf=function(filePath,update){
    var data = fs.readFileSync(filePath);
    var buf = data.toString() ;
    for(var key in update){
        buf=buf.split(key).join(update[key]);
    }
    fs.writeFileSync(filePath,buf);
}


//核心插件修改配置信息
corePlugin=function(config){
    var mobileBasicsFile = process.cwd()+'/build/core/HotUpdate/';
    var coreTmpPath = config.output+'/tmp/HotUpdate/';
    //复制核心插件
    folder.copy(mobileBasicsFile,coreTmpPath);
    //修改核心插件的配置
    var pluginXmlPath = coreTmpPath+'plugin.xml'
    replaceConf(pluginXmlPath,{
        '$SERVER_URL':config.server.url,
        '$APP_ID':config.app.id,
        '$Action_Version':config.server.action.version,
        '$Action_Map':config.server.action.map,
        '$Action_Resources':config.server.action.resources,
        '$APP_NAME':config.app.name
    });
    return coreTmpPath;
}



//添加插件
addPlugin=function(output,pluginPath){
    try{
        console.log('添加插件：' + path.basename(pluginPath));
        cordova.addPlugin(output,pluginPath);
    }catch (e){
        console.log('添加失败 : ' + e);
    }
}

//添加目录插件
var addPluginFolder = function(config){
    var pluginNames = fs.readdirSync(config.plugins);
    //添加配置插件
    for(var i in pluginNames){
        var pluginPath = path.join( config.plugins , pluginNames[i] );
        if (fs.statSync(pluginPath).isDirectory()){
             addPlugin(config.output,pluginPath);
        }
    }
}

//添加压缩包插件
var addPluginZip = function(config){
    var baseName = path.basename(config.output);
    var pluginsPath = path.join(path.dirname(config.output), baseName + '_install','plugins');
    var cmds = [];
    commandUtil.append(cmds, config.plugins , true);
    commandUtil.append(cmds, pluginsPath, true);
    javaUtil.call('UnArchive.groovy', cmds);
    //更换配置里的插件路径
    config.plugins = pluginsPath;
    console.log('解压插件包完成.');
    //调用添加目录插件的方法
    addPluginFolder(config);
}


exports.add=function(config){
    //核心插件
    addPlugin(config.output,corePlugin(config));
    //核心的webview，仅支持android
    addPlugin(config.output,modulePath+'cordova-plugin-crosswalk-webview --variable XWALK_COMMANDLINE="--disable-pull-to-refresh-effect --ignore-gpu-blacklist"');
    if (fs.existsSync(config.plugins)){
        if (fs.statSync(config.plugins).isDirectory()){
            //目录
            addPluginFolder(config);
        }else{
            //插件压缩文件
            addPluginZip(config);
        }
    }


}