var exec = require('child_process').execSync;
var commandUtil = require('./CommandUtil');
var cordovaPath = process.cwd()+'/build/nodejs/node_modules/cordova/bin/cordova';


//创建项目
exports.create=function(target , package , name , from){
    var cmds = [
        cordovaPath,
        'create',
        target,
        package,
        name,
        '--copy-from='+from
    ];
    return commandUtil.exec(cmds);
}

//添加平台
exports.addPlatform=function(target,platformSource){
    commandUtil.exec([
        cordovaPath,
        'platform',
        'add',
        platformSource
    ],target);
}

//添加插件
exports.addPlugin=function(target,pluginSource){
    commandUtil.exec([
        cordovaPath,
        'plugin',
        'add',
        pluginSource
    ],target);
}

//编译环境，平台如果为空，则编译所有环境
exports.build=function(target,type){
    //如果使用release进行编译的话，需要自己在签名。
    //--release
    commandUtil.exec([
        cordovaPath,
        'build',
        type
    ],target);
}
