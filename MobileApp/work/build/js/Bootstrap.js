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


//创建项目
platform.projects(config);

//更新config文件, 白名单与版本号
resource.updateConfig(config);

//添加平台
platform.add(config);

//添加插件
plugin.add(config);


//资源处理
resource.platform(config);

//编译环境
platform.build(config);

//打包
require('./PackageHelper').export(config);




 

