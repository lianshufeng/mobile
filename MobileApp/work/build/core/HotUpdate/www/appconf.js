var argscheck = require('cordova/argscheck'),
    channel = require('cordova/channel'),
    utils = require('cordova/utils'),
    exec = require('cordova/exec'),
    cordova = require('cordova');


function AppConf() {
     //与服务器建立连接
    this.getInfo = function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "AppConf", "getAppConfInfo", []);
    };
}

module.exports = new AppConf();
