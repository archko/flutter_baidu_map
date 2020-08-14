package com.archko.map.baidu_map;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * BaiduMapPlugin
 */
public class BaiduMapPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    private static final int REQUEST_GPS = 0x1001;
    private static final String TAG = "BaiduMapPlugin";
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Activity activity;
    private Map<String, Result> resultMap = new HashMap<>();

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        System.out.println("onAttachedToEngine:");
        channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "com.archko.map/baidu_map");
        channel.setMethodCallHandler(this);
    }

    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    public static void registerWith(Registrar registrar) {
        System.out.println("registerWith:");
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "com.archko.map/baidu_map");
        BaiduMapPlugin baiduMapPlugin = new BaiduMapPlugin();
        baiduMapPlugin.activity = registrar.activity();
        channel.setMethodCallHandler(baiduMapPlugin);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        System.out.println("onMethodCall:" + call.method);
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("getLocation")) {
            //result.success("Android " + android.os.Build.VERSION.RELEASE);
            if (null != activity) {
                resultMap.put(String.valueOf(REQUEST_GPS), result);
                Intent intent = new Intent(activity, AreaSelectorWithMapActivity.class);
                activity.startActivityForResult(intent, REQUEST_GPS);
            }
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        binding.addActivityResultListener(new PluginRegistry.ActivityResultListener() {
            @Override
            public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
                if (requestCode == REQUEST_GPS) {
                    Result result = resultMap.get(String.valueOf(REQUEST_GPS));

                    if (resultCode == Activity.RESULT_OK) {
                        String location = data.getExtras().getString("location");
                        Log.d(TAG, "data:" + location);
                        if (null != result) {
                            Map map = new HashMap();
                            map.put("code", 0);
                            map.put("data", location);
                            result.success(map);
                        }
                    } else {
                        if (null != result) {
                            Map map = new HashMap();
                            map.put("code", -1);
                            result.error("error", "error", map);
                        }
                    }
                    resultMap.remove(String.valueOf(REQUEST_GPS));
                }
                return false;
            }
        });
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {

    }

    @Override
    public void onDetachedFromActivity() {
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
}
