var commandUtil = require('./CommandUtil'),
path = require('path'),
fs = require('fs'),
jarRootPath = path.join(__dirname,'..','java','libs');


var jarLibs = function(){    
    var splitStr = process.platform.substring(0,3).toLowerCase() == 'win' ? ';':':'
    var result = path.join(__dirname,'..','libs','grovvy','groovy-all-2.4.7.jar')+ splitStr ;
    result+= jarRootPath+'/*';
    return result;
}

var jarCP = jarLibs();

/*
* 调用方法
* 脚本名
* 参数数组
*/
exports.call=function(scriptName , parameters ){
    var cmds = [];
    commandUtil.append ( cmds , "java" );
    commandUtil.append ( cmds , "-cp " + jarCP  );
    commandUtil.append ( cmds , "groovy.ui.GroovyMain" );
    commandUtil.append ( cmds , path.join(__dirname,'..','java','scripts',scriptName), true );
    for (var i in parameters){
        commandUtil.append( cmds , parameters[i] );
    }
    //生成脚本
    return commandUtil.exec(cmds);
}


