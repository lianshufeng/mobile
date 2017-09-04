//本脚本用于扫描文件的

var fs = require('fs');
var path = require('path');
var folder =  require('./Folder.js');

//扫描目录
exports.scan = function(resPath){
    var resFile = new Array();
    var fileList = new Array();
    //遍历文件
    folder.scan(resPath,fileList);
    //处理为相对路径
    for(var i = 0 ; i<fileList.length;i++){
       var relativePath = path.relative(resPath,fileList[i]);
       relativePath = relativePath.split('\\').join('/');
       relativePath = relativePath.substring(0,1)!='/'?'/'+relativePath:relativePath;
       resFile.push(relativePath);
    }
    return resFile;
};
