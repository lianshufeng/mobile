var path = require('path'),
    exec = require("child_process").execSync,
    fs = require('fs');

//nodejs的安装目录
var node_modules_path =  path.join(__dirname,'..','work','build','nodejs');

var config = {
	core: [
		'cordova',
		'-g ios-deploy'
	],
	platforms:[
		'cordova-android',
		'cordova-ios'
	],
	plugins:[
		'cordova-plugin-crosswalk-webview',
		'cordova-plugin-whitelist'
	],
	libs:[
		'image-size',
		'node-uuid'
	]
}

//安装模块
var install = function( moduleName ){
	var result = exec( 'cmd /c npm install '+ moduleName , {
		cwd: node_modules_path
	}).toString();
	return result
}

//安装数组
var installArray = function(arr){
	for(var i in arr){
		var moduleName = arr[i]
		console.log('更新 : ' + moduleName);
		try{
            var result = install(moduleName);
            console.log(result);
		}catch(e){
            console.error('err : ' + e);
		}
	}
}

//入口
var main = function(){
    //创建工作空间
    if (!fs.existsSync(node_modules_path)){
        fs.mkdir(node_modules_path);
    }
	console.log('开始更新核心模块');
	installArray(config.core);
	console.log('开始更新平台模块');
	installArray(config.platforms);
	console.log('开始更新插件模块');
	installArray(config.plugins);
	console.log('开始更新支持库模块');
	installArray(config.libs);
}

main();

