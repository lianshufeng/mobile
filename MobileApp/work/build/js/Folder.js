var fs = require('fs');
var path = require('path');

//扫描指定目录下的文件并添加到数组里
var scan = function(dirpath,fileList){
   var paths = fs.readdirSync(dirpath);
   paths.forEach(function(path) {
        var _src = dirpath + '/' + path;
        var st =  fs.statSync(_src);
        // 判断是否为文件
        if (st.isFile()) {
            fileList.push(_src);
        }
        // 如果是目录则递归调用自身
        else if (st.isDirectory()) {
            scan(_src,fileList);
        }
    });
}


//创建多级目录
mkdirs = function(dirpath) {
     if(!fs.existsSync(path.dirname(dirpath))){
            mkdirs(path.dirname(dirpath));
       }
    if(!fs.existsSync(dirpath)){
        fs.mkdirSync(dirpath);
    }
}


/*
 * 复制目录中的所有文件包括子目录
 * @param{ String } 需要复制的目录
 * @param{ String } 复制到指定的目录
 */
var copy = function(src, dst) {
    // 读取目录中的所有文件/目录
    var paths = fs.readdirSync(src);
    paths.forEach(function(path) {
        var _src = src + '/' + path,
        _dst = dst + '/' + path,
        readable, writable;
        
   var st =  fs.statSync(_src);
        // 判断是否为文件
        if (st.isFile()) {
            fs.writeFileSync(_dst,fs.readFileSync(_src));
            // 创建读取流
            //readable = fs.createReadStream(_src);
            // 创建写入流
            //writable = fs.createWriteStream(_dst);
            // 通过管道来传输流
            //readable.pipe(writable);
        }
        // 如果是目录则递归调用自身
        else if (st.isDirectory()) {
            exists(_src, _dst, copy);
        }
    
   
       
    });

};

// 在复制目录前需要判断该目录是否存在，不存在需要先创建目录
var exists = function(src, dst, callback) {
    var exists = fs.existsSync(dst);
    if (!exists) {
        fs.mkdirSync(dst);
    }
    callback(src, dst);
};

//复制文件夹
exports.copy = function(src, dst) {
    mkdirs(dst);
    return copy(src, dst);
}

//复制文件夹
exports.mkdirs = mkdirs;

//扫描目录下的文件
exports.scan = scan;

