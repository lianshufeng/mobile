var fs          =   require('fs');
var path        =   require('path');
//资源处理
var resource    =   require('./Resource');
//平台构建
var platform    =   require('./Platform.js');
//插件
var plugin = require('./Plugin');


//获取参数
var argv = process.argv;


//取出命令行
if(argv.length < 3){
    console.log('请提供配置文件的路径,本次操作被取消!');
    return ;
}

//找到配置文件
var configPath =  null;
if(fs.existsSync(argv[2])){
    configPath=argv[2];
}
if(configPath==null){
console.log('请提供有效的配置文件!');
return ;
}
configPath = fs.realpathSync(configPath);
//读取配置文件
var config = require(configPath);

//打印配置
console.log(config);

//自定义进度promise
class ProcessExecutor extends Promise {
    //构造方法
    constructor(fun){
        super(fun);
    }
    
    //异步下一个方法
    then(fun){
        return super.then(fun);
    }
    
}


//程序入口
var build = function () {
    return new ProcessExecutor(function(resolve, reject){
        if (!fs.existsSync(config.output)){
            buildProject().
            then(updateVersion).
            then(addPlatform).
            then(addPlugin).
            then(function(){
                resolve();
            })
        }else{
            resolve();
        }
    });
}

//创建项目
var buildProject = function(){
    return new ProcessExecutor(function(resolve, reject){
        platform.projects(config);
        resolve();
    });
}

//更新版本号
var updateVersion = function(){
    return new ProcessExecutor(function(resolve, reject){
        resource.updateConfig(config);
        resolve();
    });
}

//添加平台
var addPlatform = function(){
    return new ProcessExecutor(function(resolve, reject){
        platform.add(config);
        resolve();
    });
}

//添加插件
var addPlugin = function(){
    return new ProcessExecutor(function(resolve, reject){
        plugin.add(config);
        resolve();
    });
}


//更新资源
var updateResource = function(){
    return new ProcessExecutor(function(resolve, reject){
        resource.platform(config);
        resolve();
    });
}


//编译app
var buildApp = function(){
    return new ProcessExecutor(function(resolve, reject){
        platform.build(config);
        resolve();
    });
}


//打包app
var packageApp = function(){
    return new ProcessExecutor(function(resolve, reject){
        require('./PackageHelper').export(config);
        resolve();
    });
}



build().
then(updateResource).
then(buildApp).
then(packageApp)
;






/**

//文件存在
if ( !fs.existsSync(config.output) ){
    //创建项目
    platform.projects(config);

    //更新config文件, 白名单与版本号
    resource.updateConfig(config);

    //添加平台
    platform.add(config);

    //添加插件
    plugin.add(config);
}

//资源处理
resource.platform(config);

//编译环境
platform.build(config);

//打包
require('./PackageHelper').export(config);

**/


 

