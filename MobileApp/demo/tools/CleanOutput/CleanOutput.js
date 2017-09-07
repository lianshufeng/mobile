var path = require('path');
var fs = require('fs');
var folder = require('../../../work/build/js/Folder');
var promise = require('../../../work/build/nodejs/node_modules/promise');

var outputPath = path.join(__dirname, '..', '..', 'output');

//获取临时目录文件数组
var getTempDirs = function() {
    return fs.readdirSync(outputPath);
}

//是否link文件
var isLink = function(filePath) {
    try {
        fs.readlinkSync(filePath);
        return true;
    } catch(e) {
        return false;
    }
    return false;
}

//删除一个目录
var deleteFolderRecursive = function(filePath) {
    var files = [];
    if (fs.existsSync(filePath)) {
        files = fs.readdirSync(filePath);
        files.forEach(function(file, index) {
            var curPath = filePath + "/" + file;
            var linkd = isLink(curPath);
            //如果是link的目录直接跳过
            if (!linkd) {
                if (fs.statSync(curPath).isDirectory()) { // recurse
                    deleteFolderRecursive(curPath);
                } else {
                    try {
                        console.log('删除文件 : ' + curPath);
                        fs.unlinkSync(curPath);
                    } catch(e) {
                        console.log('删除失败 : ' + curPath);
                    }
                }
            }else{
                console.log('删除 Link : ' + curPath);
                fs.unlinkSync(curPath);
            }

        });
        try {
            fs.rmdirSync(filePath);
        } catch(e) {
            console.log('删除失败 : ' + filePath);
        }

    }
};

var main = function() {
    var tmpDirs = getTempDirs();
    for (var i in tmpDirs) {
        var tmpDir = tmpDirs[i];
        var tmpDirPath = path.join(outputPath, tmpDir);
        if (fs.statSync(tmpDirPath).isDirectory()){
            console.log('remove : ' + tmpDir);
            deleteFolderRecursive(tmpDirPath);
        }
        
    }
}

main();