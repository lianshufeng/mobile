/**
* 字符串工具
*/


//取字符串, 将要寻找的资源，寻找的字符起始位置，结束位置，
exports.format = function(url){
    var urlArray = url.split('://');
    var left = urlArray[0];
    var right = urlArray[1];
    right = right.split("\\").join('/');
    while(right.indexOf('//')>0){
      right = right.split("//").join('/');
    }
   return left+"://"+right;
};


