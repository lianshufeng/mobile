//
//  UodateRes.m
//
//  Created by xiaofeng on 15-1-12.
//
//

#import "UpdateRes.h"
#import "JSONKit.h"
#import "ZipHelper.h"
#import <CommonCrypto/CommonDigest.h>
#import <UIKit/UIKit.h>
#import <Cordova/CDVConfigParser.h>




@implementation UpdateRes

NSString *const wwwPath       = @"/Documents/resources/www/";
NSString *const buildPath     = @"/Documents/resources/build";
NSString *const resVerPath    = @"/Documents/resources/files.ver";
NSString *const filesHashPath = @"/Documents/resources/files.hash";


//服务端配置
NSString *const Action_VersionUrl = @"ActionVersion";
NSString *const Action_MapUrl = @"ActionMap";
NSString *const Action_ResUrl = @"ActionResources";



//app配置信息，用于缓存
NSDictionary * appConfCache =nil;
//是否正在运行这个任务，不运行重复多开
BOOL isRunTask=false;

+ (NSArray *)updateRes
{
    //判断是否已经初始化资源了，没初始化的则返回，等待初始化线程成功
    if(![self isInitResSuccess]){
        NSLog(@"未初始化资源，不允许增量更新资源");
        return nil;
    }
    NSArray* resArray  = nil;
    if(isRunTask){
        NSLog(@"正在进行资源更新操作，无需重复执行！");
        return resArray;
    }
    isRunTask=true;
    @try {
        NSLog(@"开始检查是否有新资源");
        if([self checkNewVersion] == true){
            NSDictionary * resDic = [self loadResMap];
            if (resDic != nil ){
                NSLog(@"有新资源，需要更新。");
                //load res Map
                resArray  = [self loadUpdateRes:resDic];
                NSLog(@"更新资源文件数:%d",resArray.count);
                if(resArray.count > 0){
                    //更新文件同时缓存hash
                    [self setCacheFilesHash:[self updateRes:resArray]];
                }
                //保存服务器的版本好到本地
                [self saveLocalVersion:[resDic valueForKey:@"version"]];
                NSLog(@"更新资源完成");
            }
           
        }
       
    }
    @catch (NSException *exception) {
        NSLog(@"%@",exception);
    }
    @finally {
        isRunTask=false;
    }
    return  resArray;
}



//取出路径，如果没有拷贝则拷贝
+(NSString*) getLaunchUrl{
    NSString * resRootFilePath = nil;
    BOOL initSuccess = [self isInitResSuccess];
    NSString * startPage =  [self readStartPage];
    if(initSuccess){
        resRootFilePath=[NSHomeDirectory() stringByAppendingString:wwwPath];
    }else{
        resRootFilePath= [[[NSBundle mainBundle]resourcePath] stringByAppendingString:@"/www/"];
    }
    NSString * filepath = [@"" stringByAppendingString: [resRootFilePath stringByAppendingString:startPage]];
    return filepath;
}


/**
 *资源初始化是否完成
 */
+(BOOL) isInitResSuccess{
    NSString * buildFile = [NSHomeDirectory() stringByAppendingString:buildPath];
    return [self fileExist:buildFile];
}


/**
 *初始化资源，是否需要解压压缩包
 */
+ (BOOL)initRes{
    NSDictionary *infoDictionary = [[NSBundle mainBundle] infoDictionary];
    NSString * appVersion = [infoDictionary valueForKey:@"CFBundleVersion"];
    NSString * buildFile = [NSHomeDirectory() stringByAppendingString:buildPath];
    NSString * wwwFile = [NSHomeDirectory() stringByAppendingString:wwwPath];
    BOOL exist = [self fileExist:buildFile];
    BOOL isUpdate = false;
    if(exist){
        NSString* localVer = [[NSString alloc] initWithData:[self readFile:buildFile]   encoding:NSUTF8StringEncoding];
        if(![localVer isEqualToString:appVersion]){
            isUpdate=true;
            NSLog(@"版本号不同，压缩包替换资源");
        }else{
            NSLog(@"版本好相同，无需替换资源");
        }
    }else{
        isUpdate=true;
        NSLog(@"初次安装需要迁移资源");
    }
    if(isUpdate){
        NSLog(@"正在迁移");
        //创建目录
        [self createDirectory:wwwFile];
        NSArray *files = [self readInitResFiles];
        [self copyFilesFromFiles:files target:wwwFile];
        //缓存资源文件的hash
        [self cacheFilesHash:files];
        //写安装文件 表示已安装成功
        [self writeFile:buildFile :appVersion];
        NSLog(@"迁移资源完成");
        //清空资源把本号，因为可能是更新非首次安装
        [self saveLocalVersion:@""];
    }
    
    return isUpdate;
}

//读取文件列表
+(NSArray *) readInitResFiles{
    NSString * resFiles = [[NSBundle mainBundle] pathForResource:@"AssetsList" ofType:@"json"];
    NSData* fileData = [NSData dataWithContentsOfFile:resFiles];
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:fileData options:kNilOptions error:nil];
    NSMutableArray *newArray = [[NSMutableArray alloc] init];
    for(NSString *key in json){
        [newArray addObjectsFromArray: [json objectForKey:key]];
    }
    return newArray;
}

//缓存资源文件的hash
+(void) cacheFilesHash :(NSArray*) files {
    [self setCacheFilesHash:[NSMutableSet setWithArray:files]];
}



//缓存资源文件的hash
+(void) setCacheFilesHash : (NSMutableSet*) files {
    NSString * wwwFile = [NSHomeDirectory() stringByAppendingString:wwwPath];
    NSMutableDictionary * dic = [self loadCacheFiles];
    for(NSString * filePath in files){
        NSString * fileFullPath =[wwwFile stringByAppendingString:filePath];
        //文件的hash
        NSUInteger hash =[self fileHash:fileFullPath];
        [dic setValue:@(hash) forKey:filePath];
    }
    NSData* jsonDate = [NSJSONSerialization dataWithJSONObject:dic options:NSJSONWritingPrettyPrinted error:nil];
    NSString * json = [[NSString alloc] initWithData:jsonDate encoding:NSUTF8StringEncoding];
    //保存文件
    [self writeFile:[NSHomeDirectory() stringByAppendingString:filesHashPath]:json];
}


//载入文件hash的缓存
+(NSMutableDictionary*) loadCacheFiles{
    NSMutableDictionary * dic = [NSMutableDictionary dictionary];
    NSFileManager * fileManager = [NSFileManager defaultManager];
    NSString* fileName =[NSHomeDirectory() stringByAppendingString:filesHashPath];
    if ([fileManager fileExistsAtPath:fileName]) {
        NSData* data= [self readFile:fileName];
        [dic setDictionary:[NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableLeaves error:nil]];
    }
    return dic;
}




/**
 * 拷贝目录
 */
+(void) copyFiles : (NSString*) source target:(NSString*) target{
    NSMutableArray *filesArray = [[NSMutableArray alloc] init];
    [self filesForDir:source :filesArray];
    for (int i = 0 ; i<[filesArray count]; i++) {
        NSString * fileFullPath = filesArray[i];
        NSString * fileName = [fileFullPath substringWithRange:NSMakeRange([source length],([fileFullPath length]-[source length]))];
        [self copyFile:fileFullPath toPath:[target stringByAppendingString:fileName] ];
    }
}


/**
* 根据提供的资源索引
 */
+(void) copyFilesFromFiles : (NSArray *) source target:(NSString*) target{
    NSDate * now = [NSDate date];
    NSString * appFile = [[NSBundle mainBundle]resourcePath];
    NSString * wwwPath = [appFile stringByAppendingString: @"/www/" ];
    for (int i = 0 ; i<[source count]; i++) {
        NSString * fileFullPath = [wwwPath stringByAppendingString:source[i]];
        [self copyFile:fileFullPath toPath:[target stringByAppendingString:source[i]]];
    }
    NSLog(@"case：%ld" , ((long)[[NSDate date] timeIntervalSince1970] - (long)[now timeIntervalSince1970]) );
    
    NSLog(@"%@",@"1");
    
    
}





/**
 拷贝文件
 */
+ (BOOL)copyFile:(NSString *)sourcePath toPath:(NSString *)toPath
{
    //文件夹
    NSString* pFile =  [toPath substringWithRange:NSMakeRange(0,[toPath length] -  [[toPath lastPathComponent] length] )];
    
    [[NSFileManager defaultManager] createDirectoryAtPath:pFile withIntermediateDirectories:true attributes:nil error:nil];
    
    NSFileManager *fileManger = [NSFileManager defaultManager];
    
    if ( [fileManger fileExistsAtPath:toPath] == true){
        [fileManger removeItemAtPath:toPath error:NULL ];
    }
    BOOL retVal = [fileManger copyItemAtPath:sourcePath toPath:toPath error:NULL];
    if(!retVal){
        NSLog(@"拷贝文件失败%@" , sourcePath);
    }
    return retVal;
}






//遍历指定目录下的所有文件
+(void)filesForDir:(NSString*)path :(NSMutableArray*) fileArray
{
    NSFileManager *fileManager = [[NSFileManager alloc] init];
    
    NSArray* array = [fileManager contentsOfDirectoryAtPath:path error:nil];
    for(int i = 0; i<[array count]; i++)
    {
        NSString *fullPath = [path stringByAppendingPathComponent:[array objectAtIndex:i]];
        BOOL isDir;
        if ( !([fileManager fileExistsAtPath:fullPath isDirectory:&isDir] && isDir) )
        {
            //NSDictionary *fileAttributeDic=[fileManager attributesOfItemAtPath:fullPatherror:nil];
            //size+= fileAttributeDic.fileSize;
            //NSLog(fullPath);
            [fileArray addObject:fullPath];
        }
        else
        {
            [self filesForDir:fullPath :fileArray];
        }
    }
    
    
}




/**
 *解压文件
 */
+(void) unzipFiles : (NSString *) sourceZip targetFile : (NSString *) target updateFiles :(NSMutableSet*) updateFiles {
    ZipHelper* zip = [[ZipHelper alloc] init];
    if( [zip UnzipOpenFile:sourceZip] )
    {
        BOOL ret = [zip UnzipFileTo:target overWrite:YES files:updateFiles];
        if( NO==ret ){
        }
        [zip UnzipCloseFile];
    }
    
}

/**
 *日期转字符串
 */
+ (NSString *)dateFromString:(NSDate *)date{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    NSString *strDate = [dateFormatter stringFromDate:date];
    return strDate;
}



/**
 * 文件是否存在
 */
+(bool) fileExist:(NSString*)filename {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    BOOL isDirExist = [fileManager fileExistsAtPath:filename ];
    return isDirExist;
}

/**
 *创建目录
 */
+(void) createDirectory : (NSString*) fileName{
    NSFileManager *fileManager = [NSFileManager defaultManager];
    [fileManager createDirectoryAtPath:fileName withIntermediateDirectories:YES attributes:nil error:nil];
}


/**
 *  写文件
 */
+(bool)writeFile:(NSString*)filename   :(NSString*)data{
    NSData* nsData = [data dataUsingEncoding:NSUTF8StringEncoding];
    return  [nsData writeToFile:filename atomically:YES];
}


/**
 *读取文件
 */
+(NSData*) readFile : (NSString*)fileName{
    NSData * data = [NSData dataWithContentsOfFile:fileName];
    return data;
}


/**
 Has New a Version
 */
+(BOOL) checkNewVersion {
    NSString * serverNewVersion = [self loadServerVersion];
    if (serverNewVersion == nil){
        return false;
    }
    return ![[self loadLocalVersion] isEqualToString:serverNewVersion];
}


/**
 *更新资源文件
 */
+(NSMutableSet*) updateRes:(NSArray*) resArray{
    NSString * resRootFilePath = [NSHomeDirectory() stringByAppendingString:wwwPath];
    NSString*  hostUrl = [self createActionUrl:Action_ResUrl];
    NSString * postInfo = @"";
    for (int i = 0 ; i<resArray.count ; i++) {
        postInfo = [postInfo stringByAppendingString:@"fileNames="];
        postInfo = [postInfo stringByAppendingString:resArray[i]];
        postInfo = [postInfo stringByAppendingString:@"&"];
    }
    //更新资源文件
    NSData * data = [self httpReadData:hostUrl httpPost:true httpPostInfo:postInfo];
    NSString * resZipFile = [NSTemporaryDirectory() stringByAppendingString:@"/updateRes.zip"];
    //保存到临时文件里
    [data writeToFile:resZipFile atomically:true];
    //解压文件完成替换资源
    NSMutableSet * updateFiles = [NSMutableSet set];
    [self unzipFiles:resZipFile targetFile:resRootFilePath updateFiles:updateFiles];
    return updateFiles;
}

/**
 *载入服务器获取资源
 */
+(NSString * ) loadServerVersion{
    NSString * url = [self createActionUrl:Action_VersionUrl];
    NSString * data = [self httpReadDocument:url httpPost:true httpPostInfo:@""];
    //JSON字符串转对象
    NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:[ data dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingMutableLeaves error:nil];
    return [dic valueForKey:@"version"];
}




/**
 * 载入更新资源
 */
+(NSArray *) loadUpdateRes : (NSDictionary * )dic {
    NSArray *array = [NSArray arrayWithObjects: nil];
    NSDictionary * map = [dic valueForKey:@"map"];
    NSDate * now = [NSDate date];
    NSMutableDictionary*  localDic =  [self loadCacheFiles];
    for(NSString * key in map){
        NSString* localFileHash =  [localDic objectForKey:key];
        if(![localFileHash isEqualToString:map[key]]){
            array = [array arrayByAddingObject:key];
        }
        
    }
    NSLog(@"扫描全局文件开销：%ld" , ((long)[[NSDate date] timeIntervalSince1970] - (long)[now timeIntervalSince1970]) );
    return array;
}









/**
 * 载入指定版本的资源地图
 */
+(NSDictionary *) loadResMap{
    NSString * url = [self createActionUrl:Action_MapUrl];
    NSString * data = [self httpReadDocument:url httpPost:true httpPostInfo:@""];
    //JSON字符串转对象
    NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:[ data dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingMutableLeaves error:nil];
    return dic;
}



/**
 * load local version
 */
+(NSString *) loadLocalVersion{
    NSData* data = [self readFile:[NSHomeDirectory() stringByAppendingString:resVerPath]];
    return [[NSString alloc] initWithData:data  encoding:NSUTF8StringEncoding];
}

/**
 * save local version
 */
+(void ) saveLocalVersion : (NSString *) value {
    [self writeFile:[NSHomeDirectory() stringByAppendingString:resVerPath] :value];
}



//读取服务端的HostUrl
+(NSString *) getServerHostUrl{
    NSDictionary *nd = [self getAppConf];
    NSString* hostUrl =  [nd valueForKey:@"ServerUrl"];
    return hostUrl;
}

//取出应用id
+(NSString *) getAppId{
    NSDictionary *nd = [self getAppConf];
    NSString* appId =  [nd valueForKey:@"MobileAppid"];
    return appId;
}


/**
 * http访问网络文档
 */
+(NSString*) httpReadDocument:(NSString *) hostUrl httpPost:(bool)isPost httpPostInfo:(NSString *) postInfo {
    NSData *received = [self httpReadData:hostUrl httpPost:isPost httpPostInfo:postInfo];
    NSString *result = [[NSString alloc]initWithData:received encoding:NSUTF8StringEncoding];
    return result;
}



/**
 * http读数据文件
 */
+(NSData*)httpReadData:(NSString *) hostUrl httpPost:(bool)isPost httpPostInfo:(NSString *) postInfo {
    NSURL *url  = [NSURL URLWithString:hostUrl] ;
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc]initWithURL:url cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:10];
    if(isPost){
        [request setHTTPMethod:@"POST"];//设置请求方式为POST，默认为GET
    }
    NSString *str = postInfo;//设置参数
    NSData *data = [str dataUsingEncoding:NSUTF8StringEncoding];
    [request setHTTPBody:data];
    NSData *received = [NSURLConnection sendSynchronousRequest:request returningResponse:nil error:nil];
    return received;
}


//read sys config value
+(NSString *) readSysConfigValue:(NSString *)key {
    
    CDVConfigParser* delegate = [[CDVConfigParser alloc] init];
    
    // read from config.xml in the app bundle
    NSString* path = [[NSBundle mainBundle] pathForResource:@"config" ofType:@"xml"];
    if (![[NSFileManager defaultManager] fileExistsAtPath:path]) {
        NSAssert(NO, @"ERROR: config.xml does not exist. Please run cordova-ios/bin/cordova_plist_to_config_xml path/to/project.");
        return nil;
    }
    NSURL* url = [NSURL fileURLWithPath:path];
    
    NSXMLParser* configParser = [[NSXMLParser alloc] initWithContentsOfURL:url];
    if (configParser == nil) {
        NSLog(@"Failed to initialize XML parser.");
        return nil;
    }
    [configParser setDelegate:((id < NSXMLParserDelegate >)delegate)];
    [configParser parse];
    
    return delegate.startPage;
    
}





//read sys config value
+(NSString *) readStartPage {
    CDVConfigParser* delegate = [[CDVConfigParser alloc] init];
    
    // read from config.xml in the app bundle
    NSString* path = [[NSBundle mainBundle] pathForResource:@"config" ofType:@"xml"];
    if (![[NSFileManager defaultManager] fileExistsAtPath:path]) {
        NSAssert(NO, @"ERROR: config.xml does not exist. Please run cordova-ios/bin/cordova_plist_to_config_xml path/to/project.");
        return nil;
    }
    NSURL* url = [NSURL fileURLWithPath:path];
    
    NSXMLParser* configParser = [[NSXMLParser alloc] initWithContentsOfURL:url];
    if (configParser == nil) {
        NSLog(@"Failed to initialize XML parser.");
        return nil;
    }
    [configParser setDelegate:((id < NSXMLParserDelegate >)delegate)];
    [configParser parse];
    
    return delegate.startPage;
    
}


//取出应用配置信息
+(NSDictionary*) getAppConf{
    if(!appConfCache){
        NSString *plistPath = [[NSBundle mainBundle] pathForResource:@"" ofType:@"plist"];
        NSMutableDictionary *data = [[NSMutableDictionary alloc] initWithContentsOfFile:plistPath];
        appConfCache =  [data valueForKey:@"AppConf"];
    }
    return appConfCache;
}


/**
 从文档目录中读取某个文件的配置，只支持plist
 */
+(NSString *) readConfigValue:(NSString *)key    readFile: (NSString *) fileName {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];//获取标准函数对象
    NSString * value = [defaults valueForKey:[fileName stringByAppendingString:key]];
    [defaults synchronize];
    return value;
    
}

/**
 * 保存配置值
 *
 */
+(void) writeConfigKey:(NSString *) key writeConfigValue:(NSString *) value readFile: (NSString *) fileName {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];//获取标准函数对象
    [defaults setValue:value forKey:[fileName stringByAppendingString:key]];
    [defaults synchronize];
}


/**
 *获取文件属性
 */
+(NSDictionary*) getFileAttributes:(NSString *) fileName {
    NSDictionary *fileDictionary = [[NSFileManager defaultManager] attributesOfItemAtPath:fileName error:nil];
    return fileDictionary;
}



/**
 *取文件hash
 */
+(NSUInteger*)fileHash:(NSString*)fileName
{
    NSData * data = [NSData dataWithContentsOfFile:fileName];
    uLong crc = crc32(0, NULL, 0);
    return crc32(crc, data.bytes,data.length);
//    unsigned int creValue   = crc32(crc, data.bytes,data.length);
//    unsigned int creValue  = crc32(crc, data.bytes,data.length);
//    char buf[16] = {0};
//    sprintf(buf, "%x", creValue);
//    NSString *hash = [ [NSString alloc] initWithUTF8String:buf];
//    return hash;
//    return creValue;
}





/**
 *创建请求的方法
 */
+(NSString*) createActionUrl:(NSString*) actionName {
    NSDictionary *nd = [self getAppConf];
    NSString* hostUrl =  [nd valueForKey:@"ServerUrl"];
    NSString* uri = [nd valueForKey:actionName];
    NSString* appId =  [nd valueForKey:@"MobileAppid"];
    
    //url space str
    NSString* urlSpace =  [[uri substringToIndex:1] isEqualToString:@"/"] ? @"":@"/";
    NSString* requestUrl = [hostUrl stringByAppendingString:urlSpace ];
    requestUrl = [requestUrl stringByAppendingString:uri];
    NSString* parameterSpace = ([uri rangeOfString:@"?"].length > 0) ? @"&":@"?";
    requestUrl = [requestUrl stringByAppendingString:parameterSpace];
    requestUrl = [requestUrl stringByAppendingString:@"appId="];
    requestUrl = [requestUrl stringByAppendingString:appId];
    return requestUrl;
}




@end
