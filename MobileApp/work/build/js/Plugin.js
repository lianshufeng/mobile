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
    var mobileBasicsFile = path.join(process.cwd(),'build','core','HotUpdate');
    var coreTmpPath = path.join(config.output+'_install','plugins','HotUpdate');
    //复制核心插件
    folder.copy(mobileBasicsFile,coreTmpPath);
    //修改核心插件的配置
    var pluginXmlPath = path.join(coreTmpPath,'plugin.xml')
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


corePlugin_bak=function(config){
    var hotUpdatePluginPath = path.join(process.cwd(),'build','core','HotUpdate');
    var pluginConfig = {
        'SERVER_URL':config.server.url,
        'APP_ID':config.app.id,
        'Action_Version':config.server.action.version,
        'Action_Map':config.server.action.map,
        'Action_Resources':config.server.action.resources,
        'APP_NAME':config.app.name
    };
    var cmds = '';
    for (var i in pluginConfig){
        cmds += ' --variable ' + i+'="' + pluginConfig[i] + '"';
    }
    return [hotUpdatePluginPath , cmds];
}



//添加插件
addPlugin=function(output , pluginPath , cmd){
    try{
        var cmds = cmd?' '+cmd:''
        console.log('添加插件：' + path.basename(pluginPath) + ' ' + cmds );
        cordova.addPlugin(output , pluginPath + ' ' + cmds);
    }catch (e){
        console.log('添加失败 : ' + e);
    }
}

//添加目录插件
var addPluginFolder = function(config){
    var pluginNames = fs.readdirSync(config.plugins.path);
    //添加配置插件
    for(var i in pluginNames){
        var pluginName = pluginNames[i];
        var variables = new Array();
        if ( config.plugins && config.plugins.variables && config.plugins.variables[pluginName] ){
            var pluginVar = config.plugins.variables[pluginName];
            for (var varKey in pluginVar ){
                var variable = '--variable ' + varKey + '=' +'"'+pluginVar[varKey] + '"';
                variables.push(variable);
            }
        }
        //追加变量列表
        var pluginPath = path.join( config.plugins.path , pluginName ) ;
        if (fs.statSync(pluginPath).isDirectory()){
             addPlugin( config.output , pluginPath , variables.join(' ') );
        }
    }
}

//添加压缩包插件
var addPluginZip = function(config){
    var baseName = path.basename(config.output);
    var pluginsPath = path.join(path.dirname(config.output), baseName + '_install','plugins');
    var cmds = [];
    commandUtil.append(cmds, config.plugins.path , true);
    commandUtil.append(cmds, pluginsPath, true);
    javaUtil.call('UnArchive.groovy', cmds);
    //更换配置里的插件路径
    config.plugins.path = pluginsPath;
    console.log('解压插件包完成.');
    //调用添加目录插件的方法
    addPluginFolder(config);
}

//添加插件入口
exports.add=function(config){
    //核心插件
    addPlugin( config.output , corePlugin(config) );
    //添加扩展插件
    if (config.plugins && config.plugins.path && fs.existsSync(config.plugins.path) ){
        if (fs.statSync(config.plugins.path).isDirectory()){
            //目录
            addPluginFolder(config);
        }else{
            //插件压缩文件
            addPluginZip(config);
        }
    }
}