var path = require('path'),
    os = require('os'),
    exec = require("child_process").exec,
    fs = require('fs'),
    spawn = require('child_process').spawn;
    

//nodejs的安装目录
var node_modules_path =  path.join(__dirname,'..','work','build','nodejs');

var config = {
	core: [
		'cordova',
		'-g ios-deploy'
	],
	platforms:[
		'cordova-android@6.3.0',
		'cordova-ios'
	],
	plugins:[
		'cordova-plugin-whitelist'
	],
	libs:[
		'image-size',
		'node-uuid',
		'promise',
		'plist'
	]
}

//安装模块
var install = function( moduleName ){
	var result = exec( 'npm install '+ moduleName , {
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
    /**
	console.log('开始更新核心模块');
	installArray(config.core);
	console.log('开始更新平台模块');
	installArray(config.platforms);
	console.log('开始更新插件模块');
	installArray(config.plugins);
	console.log('开始更新支持库模块');
	installArray(config.libs);
	**/
	

    var npmPath = path.join(path.dirname(process.execPath),'npm');;
    if (os.platform().indexOf('win') > -1){
        npmPath = npmPath+".cmd"
    }
    
	var install = spawn( npmPath , ['install'] ,{'cwd': node_modules_path});
	install.stdout.on('data', function (data) {
        console.log(data.toString());
    });

    // 捕获标准错误输出并将其打印到控制台
    install.stderr.on('data', function (data) {
        console.error(data.toString());
    });

    // 注册子进程关闭事件
    install.on('exit', function (code, signal) {
        console.log('finish');
    });
	
	/**
	exec('npm install', {cwd: node_modules_path}, function (error, stdout, stderr) {
        console.log(stdout);
    });
    **/
}

main();

