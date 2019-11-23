#import "NcImagePickerPlugin.h"
#import <nc_image_picker/nc_image_picker-Swift.h>

@implementation NcImagePickerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftNcImagePickerPlugin registerWithRegistrar:registrar];
}
@end
