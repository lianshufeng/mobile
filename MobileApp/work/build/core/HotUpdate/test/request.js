$(function(){
    var request = $.request;

    //第一次请求需要设置HOST
    //request.setHost('http://192.168.1.105:8080/MServer');
    //需要一次正确的登陆
    /**
    request.login({username:'xiaofeng',password:'pd123456'},function(content){
        console.log(content);
    },function(content){
        console.log(content);
    });
    */
    //访问接口的例子
    /**
    request.get({
        url:'proxy/get',
        data:{
            url:'http://www.baidu.com'
        },
        success:function(data){
            console.log(data);
        }
    });
    */
    console.log($("*"));
    
});