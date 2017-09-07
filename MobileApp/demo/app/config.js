var path = require('path');


/**
*配置文件
*/

//  Demo 的资源路径
var demoPath = __dirname;

var config = {
    app:{
        name:'快速移动',
        package:'com.fast.dev.app.demo',
        id:'phone',
        version:'1.0.0',
        icon:path.join(demoPath,'app.png'),
        crosswalk:true
    },
    server:{
        url:'http://172.18.21.238:8080/PServer',
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
    output : path.join(demoPath,'..','output',new Date().getTime().toString()),
    project: path.join(demoPath,'app.zip'),
    plugins: path.join(demoPath,'plugins.rar'),
    pushApiKey:{
        android:'TLdXLOb1lnHr2HGZhxnPikxb',
        ios:'zi0yKSP2qGhqPmPLgIodBNKp'
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






