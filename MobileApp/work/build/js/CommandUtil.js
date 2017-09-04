var exec = require('child_process').execSync;

//数组到命令行
var toCommandLine = function(parameters){
    var cmds = "";
    for (var i in parameters){
        cmds += parameters[i];
        cmds += " "
    }
    return cmds;
}


/*
* 追加参数到数组
*/
exports.append=function( arr , cmd , isPath ){
    //替换符号
    var val = cmd.split("\\").join("/");
    return arr.push(val);
}

/*
* 调用方法
* 脚本名
* 参数数组
*/
exports.exec=function( parameters,cwd){
    var cmds = toCommandLine(parameters);
    var rt ;
    if(cwd){
        rt = exec(cmds,{"cwd":cwd});
    }else{
        rt = exec(cmds);
    }
    return rt.toString();
}


exports.toCommandLine = toCommandLine;


