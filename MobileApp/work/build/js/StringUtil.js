/**
* 字符串工具
*/


//取字符串, 将要寻找的资源，寻找的字符起始位置，结束位置，
exports.subString = function (src,start,end,index){
    if(!index){index=0}
    var at1 = src.indexOf(start,index);
    var at2 = src.indexOf(end,at1+start.length);
    return src.substring(at1+start.length,at2);
};
