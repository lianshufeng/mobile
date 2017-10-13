var path = require('path');


/**
*配置文件
*/

//  Demo 的资源路径
var demoPath = __dirname;

var config = {
    app:{
        name:'测试App',
        package:'com.fast.dev.app.demo.local',
        id:'development_user',
        version:'1.0.0',
        icon:path.join(demoPath,'app.png'),
        launch:{
            backgroup:path.join(demoPath,'launch_backgroup.jpg'),
            scale:0.382,
            icon:path.join(demoPath,'launch_icon.png')
        }
    },
    server:{
        url:'http://192.168.0.110:8080/PServer',
        action:{
            version : '/HotUpdate/getVersion',
            map : '/HotUpdate/getMap',
            resources: '/HotUpdate/getRes'
        }
    },
    sign:{
        ios:{
            developmentTeam:'67C9MWUC27',
            packageType:'development',
            developmentProfile:path.join(demoPath,'sign','iosDevelopment.mobileprovision'),
            distributionProfile:path.join(demoPath,'sign','iosDistribution.mobileprovision')
        },
        android:{
            keystore:path.join(demoPath,'sign','android.keystore'),
            storePassword:'bajievr',
            alias:'vr',
            password:'bajievr'
        }
    },
    output : path.join(demoPath,'..','output','test'),
    project: path.join(demoPath,'app.zip'),
    plugins: {
        path : path.join(demoPath,'plugins.rar'),
        variables : {
            'cordova-plugin-baidumaplocation' : {
                'ANDROID_KEY':'TLdXLOb1lnHr2HGZhxnPikxb',
                'IOS_KEY':'zi0yKSP2qGhqPmPLgIodBNKp'
            },
            'cordova-plugin-crosswalk-webview' : {
                'XWALK_COMMANDLINE':'--disable-pull-to-refresh-effect --ignore-gpu-blacklist'
            }
        }
    }
}

module.exports = config;



/*
参数备注:
    ios 签名
    config.sign.ios={
        developmentTeam : 开发者组的ID ,
        packageType: 包的类型 : development enterprise ad-hoc app-store ,
        distributionProfile: 签名发布的配置文件，通过ios开发者申请, 该文件必须匹配开发者id,
        developmentProfile:  签名开发的配置文件
    }
*/


/*
建议插件:
    cordova-plugin-appversion 显示app版本
    cordova-plugin-crosswalk-webview Android 独立浏览器内核
*/


/**
启动界面
config.app.launch = {
    backgroup:#若#第一个字符则为RGB十六进制，为数字则为RGB十进制,或者为背景图的文件路径
    scale:相对背景的缩放比例
    icon:背景图中的图标
}
*/



