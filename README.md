# mobile
#### 尽量使用最新的稳定版

1、安装 nodejs ， 并配置环境变量 
node -v

2、安装 JDK , 并配置环境变量
java -version


3、安装 android sdk，并配置环境变量:
%ANDROID_HOME%
adb

注意：更新 licenses，否则Dradle 打包失败
./sdkmanager --update
./sdkmanager --licenses



4、安装 gradle ，并设置环境变量：
gradle -version

Build time:   2017-03-03 19:45:41 UTC
Revision:     9eb76efdd3d034dc506c719dac2955efb5ff9a93

Groovy:       2.4.7
Ant:          Apache Ant(TM) version 1.9.6 compiled on June 29 2015
JVM:          1.8.0_111 (Oracle Corporation 25.111-b14)
OS:           Windows 7 6.1 amd64



####MAC 环境变量
vim ~/.bash_profile

export PATH=$PATH:/zbj_mobile/sdk/gradle/bin
export ANDROID_HOME=/zbj_mobile/sdk/android-sdk-macosx
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:/zbj_mobile/mobile/work/build/nodejs/node_modules/cordova/bin






#### IOS证书记录：
1、xcode -> preferences -> Accounts 增加开发者账号
    证书管理,增加 ios development 证书， 增加 IOS 发布证书(重要)

2、General -> Signing -> Tean



证书申请：
1、App ids ... 
2、Provisioning Profiles -> Distribution -> App store -> App ID -> Select certificates -> download


developmentTeam 参数是 Team ID

签名：--buildConfig="build.json"


