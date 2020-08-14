import 'dart:async';

import 'package:flutter/services.dart';

class BaiduMap {
  static const MethodChannel _channel =
      const MethodChannel('com.archko.map/baidu_map');

  static Future<T> invokeMethod<T>(String method, [dynamic arguments]) async {
    return await _channel.invokeMethod<T>(method, arguments);
  }

  static Future<List<T>> invokeListMethod<T>(String method,
      [dynamic arguments]) async {
    return await _channel.invokeListMethod<T>(method, arguments);
  }

  static Future<Map<K, V>> invokeMapMethod<K, V>(String method,
      [dynamic arguments]) async {
    return await _channel.invokeMapMethod<K, V>(method, arguments);
  }

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<Map> getLocation() async {
    final Map address = await invokeMapMethod('getLocation');
    //{code: 0, data: {"address":"北京市东城区广场西侧路","lat":39.91108923635131,"lng":116.40275584463448}}
    print("getLocation:$address");
    return address;
  }
}
