var fs = require('fs'),
path = require('path'),
folder = require('./folder.js'),
cordova = require('./cordova'),
javaUtil = require('./JavaUtil'),
commandUtil = require('./CommandUtil'),
modulePath = process.cwd()+'/build/nodejs/node_modules/';


//简单的拷贝对象
var mergetObject = function ( objs ) {
    var result = {};
    //对象1
    for ( var i in objs ) {
        var obj = objs[i];
        if (obj){
            for (var key in obj){
                result[key] = obj[key];
            }
        }
    }
    return result;
}

/**
* 核心插件默认配置
*/
var coreDefaultPlugins = {
    'HotUpdate': {
        'SERVER_URL': null,
        'APP_ID': null,
        'APP_NAME': null,
        'PACKAGE_NAME': null,
        'Action_Version': '/HotUpdate/getVersion',
        'Action_Map': '/HotUpdate/getMap',
        'Action_Resources': '/HotUpdate/getRes'
    },
    'UpdateIosAuth': {
        'NSPhotoLibraryUsageDescription': 'App需要您的同意,才能访问相册',
        'NSCameraUsageDescription': 'App需要您的同意,才能访问相机',
        'NSMicrophoneUsageDescription': 'App需要您的同意,才能访问麦克风',
        'NSLocationUsageDescription': 'App需要您的同意,才能访问位置',
        'NSLocationWhenInUseUsageDescription': 'App需要您的同意,才能在使用期间访问位置',
        'NSLocationAlwaysUsageDescription': 'App需要您的同意,才能始终访问位置',
        'NSCalendarsUsageDescription': 'App需要您的同意,才能访问日历',
        'NSRemindersUsageDescription': 'App需要您的同意,才能访问提醒事项',
        'NSMotionUsageDescription': 'App需要您的同意,才能访问运动与健身',
        'NSHealthUpdateUsageDescription': 'App需要您的同意,才能访问健康更新',
        'NSHealthShareUsageDescription': 'App需要您的同意,才能访问健康分享',
        'NSBluetoothPeripheralUsageDescription': 'App需要您的同意,才能访问蓝牙',
        'NSAppleMusicUsageDescription': 'App需要您的同意,才能访问媒体资料库'
    }
}


/**
*插件配置构造工厂
*/
var coreConfigPluginFactory = {
    'HotUpdate':function(config,pluginPath){
        var conf = {
            'SERVER_URL':config.server.url,
            'APP_ID':config.app.id,
            'Action_Version':config.server.action.version,
            'Action_Map':config.server.action.map,
            'Action_Resources':config.server.action.resources,
            'APP_NAME':config.app.name,
            'PACKAGE_NAME':config.app.package
        };
        return mergetObject( [coreDefaultPlugins['HotUpdate'],conf] );
    },
    'UpdateIosAuth':function(config,pluginPath) {
        return mergetObject( [ coreDefaultPlugins['UpdateIosAuth'] , config.app['usageDescription'] ]);
    }
}


//替换配置信息
var replaceConf=function(filePath,update){
    var data = fs.readFileSync(filePath);
    var buf = data.toString() ;
    for(var key in update){
        buf=buf.split('$'+key).join(update[key]);
    }
    fs.writeFileSync(filePath,buf);
}

//核心插件修改配置信息
var corePlugin = function( config,order,pluginName ){
    var mobileBasicsFile = path.join(process.cwd(),'build','core' , order , pluginName);
    var pluginPath = path.join(config.output+'_install','plugins' , order , pluginName);    
    //复制核心插件
    folder.copy(mobileBasicsFile,pluginPath);
    //合并用户配置参数
    var userConfig = coreConfigPluginFactory[pluginName](config,pluginPath);
    //插件配置路径
    var pluginXmlPath = path.join(pluginPath,'plugin.xml');
    //替换并保存插件
    replaceConf(pluginXmlPath,userConfig);
    
    /**
    //修改核心插件的配置
    var pluginXmlPath = path.join(coreTmpPath,'plugin.xml');
    //更换插件
    replaceConf(pluginXmlPath,{
        'SERVER_URL':config.server.url,
        'APP_ID':config.app.id,
        'Action_Version':config.server.action.version,
        'Action_Map':config.server.action.map,
        'Action_Resources':config.server.action.resources,
        'APP_NAME':config.app.name,
        'PACKAGE_NAME':config.app.package
    });
    
    
    
    //修改配置
    var usageDescriptionMap = config.app['usageDescription']  ; 
    if (usageDescriptionMap){
        replaceConf(pluginXmlPath,usageDescriptionMap);
    }
    **/
   
    
    return pluginPath;
}


//添加插件
var addPlugin = function(output , pluginPath , cmd){
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
    var pluginsPath = path.join(path.dirname(config.output), baseName + '_install','plugins','post');
    var cmds = [];
    commandUtil.append(cmds, config.plugins.path , true);
    commandUtil.append(cmds, pluginsPath, true);
    javaUtil.call('UnArchive.groovy', cmds);
    //更换配置里的插件路径
    config.plugins.path = pluginsPath;
    //调用添加目录插件的方法
    addPluginFolder(config);
}

//添加核心插件
var addCorePlugin = function( order , config ) {
    var corePluginPath = path.join( process.cwd() , 'build' , 'core' , order);
    var plugins = fs.readdirSync(corePluginPath);
    for (i in plugins) {
        addPlugin( config.output , corePlugin( config , order , plugins[i] ) );
    }
}

//添加插件入口
exports.add = function(config){
    //核心插件
    addCorePlugin('pre' , config);
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
     //核心插件
    addCorePlugin('after' , config);
}