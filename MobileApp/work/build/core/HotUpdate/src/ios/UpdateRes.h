//  本工具实现资源文件的自动更新功能，必须专有的服务端的支持
//  Header.h
//  MobileAppGHMobileIphone
//
//  Created by xiaofeng on 15-1-12.
//
//


#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>


@interface UpdateRes : NSObject
+(NSArray *) updateRes;
+(BOOL) initRes;
+(NSString*) getLaunchUrl;
+(NSDictionary *) loadServerConf;
@end

