/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

#include <sys/types.h>
#include <sys/sysctl.h>

#import <Cordova/CDV.h>
#import "CDVAppConf.h"

@implementation UIDevice (ModelVersion)

- (NSString*)modelVersion
{
    size_t size;

    sysctlbyname("hw.machine", NULL, &size, NULL, 0);
    char* machine = malloc(size);
    sysctlbyname("hw.machine", machine, &size, NULL, 0);
    NSString* platform = [NSString stringWithUTF8String:machine];
    free(machine);

    return platform;
}

@end

@interface CDVAppConf () {}
@end

@implementation CDVAppConf

- (NSString*)uniqueAppInstanceIdentifier:(UIDevice*)device
{
    NSUserDefaults* userDefaults = [NSUserDefaults standardUserDefaults];
    static NSString* UUID_KEY = @"CDVUUID";

    NSString* app_uuid = [userDefaults stringForKey:UUID_KEY];

    if (app_uuid == nil) {
        CFUUIDRef uuidRef = CFUUIDCreate(kCFAllocatorDefault);
        CFStringRef uuidString = CFUUIDCreateString(kCFAllocatorDefault, uuidRef);

        app_uuid = [NSString stringWithString:(__bridge NSString*)uuidString];
        [userDefaults setObject:app_uuid forKey:UUID_KEY];
        [userDefaults synchronize];

        CFRelease(uuidString);
        CFRelease(uuidRef);
    }

    return app_uuid;
}

- (void)getAppConfInfo:(CDVInvokedUrlCommand*)command
{
    NSDictionary* deviceProperties = [self deviceProperties];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:deviceProperties];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (NSDictionary*)deviceProperties
{
    NSMutableDictionary* appConfProps = [NSMutableDictionary dictionaryWithCapacity:4];
    //取配置数据
    NSDictionary* nd = [self  getAppConf];
    [appConfProps setObject:[nd valueForKey:@"CFBundleName"] forKey:@"name"];
    [appConfProps setObject:[nd valueForKey:@"CFBundleIdentifier"] forKey:@"package"];
    [appConfProps setObject:[self getAppId:nd] forKey:@"appId"];
    [appConfProps setObject:[nd valueForKey:@"CFBundleVersion"] forKey:@"appVer"];
    [appConfProps setObject:[self getServerUrl:nd] forKey:@"serverUrl"];
    NSDictionary* confReturn = [NSDictionary dictionaryWithDictionary:appConfProps];
    return confReturn;
}




//取出应用配置信息
-(NSDictionary*) getAppConf{
    NSString *plistPath = [[NSBundle mainBundle] pathForResource:@"" ofType:@"plist"];
    NSMutableDictionary *data = [[NSMutableDictionary alloc] initWithContentsOfFile:plistPath];
    return data;
}



//读取服务端的HostUrl
-(NSString *) getServerUrl:(NSDictionary*) nd {
    NSString* hostUrl =  [[nd valueForKey:@"AppConf"] valueForKey:@"Mobile.url"];
    return hostUrl;
}

//取出应用id
-(NSString *) getAppId:(NSDictionary*) nd {
    NSString* appId =  [[nd valueForKey:@"AppConf"] valueForKey:@"Mobile.appId"];
    return appId;
}




+ (NSString*)cordovaVersion
{
    return CDV_VERSION;
}

@end
