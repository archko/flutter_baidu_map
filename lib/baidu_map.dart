import 'dart:async';

import 'package:flutter/services.dart';

class BaiduMap {
  static const MethodChannel _channel =
      const MethodChannel('com.archko.map/baidu_map');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String> getAddress() async {
    final String address = await _channel.invokeMethod('getAddress');
    print("getAddress:$address");
    return address;
  }
}
