package com.example.my_pda_scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import java.util.Map;
import java.util.HashMap;
import org.json.JSONObject;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * MyPdaScannerPlugin
 */
public class MyPdaScannerPlugin implements FlutterPlugin {

    private EventChannel eventChannel;
    private MethodChannel flutterChannel;
    private Context applicationContext;

    private static String ACTION_DATA_CODE_RECEIVED = "";
    private static String DATA = "";
    private static Map<String, String> DATA_MAP = new HashMap<>();

    private static final String CHARGING_CHANNEL = "my_pda_channel";
    private static final String FLUTTER_TO_ANDROID_CHANNEL = "flutter_to_android";

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        flutterChannel = new MethodChannel(
                flutterPluginBinding.getBinaryMessenger(), FLUTTER_TO_ANDROID_CHANNEL);
        flutterChannel.setMethodCallHandler(new MethodChannel.MethodCallHandler() {
            @Override
            public void onMethodCall(MethodCall call, Result result) {
                if (call.method.equals("sendMessage")) {
                    String pda_action = call.argument("pda_action");
                    String qr_data_tag = call.argument("qr_data_tag");
                    String image_data_tag = call.argument("image_data_tag");
                    String ocr_data_tag = call.argument("ocr_data_tag");
                    ACTION_DATA_CODE_RECEIVED = pda_action;
                    // DATA = data_tag;
                    DATA_MAP.put("qr_data_tag", qr_data_tag);
                    DATA_MAP.put("image_data_tag", image_data_tag);
                    DATA_MAP.put("ocr_data_tag", ocr_data_tag);

                    result.success(null); 
                    } else {
                    result.notImplemented();
                }
            }
        });

        eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), CHARGING_CHANNEL);
        eventChannel.setStreamHandler(new EventChannel.StreamHandler() {

            private BroadcastReceiver chargingStateChangeReceiver;

            @Override
            public void onListen(Object arguments, EventChannel.EventSink events) {
                chargingStateChangeReceiver = createChargingStateChangeReceiver(events);
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_DATA_CODE_RECEIVED);
                applicationContext.registerReceiver(
                        chargingStateChangeReceiver, filter);
            }

            @Override
            public void onCancel(Object arguments) {
                applicationContext.unregisterReceiver(chargingStateChangeReceiver);
                chargingStateChangeReceiver = null;
            }
        });

        applicationContext = flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        eventChannel.setStreamHandler(null);
    }


    private BroadcastReceiver createChargingStateChangeReceiver(final EventChannel.EventSink events) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // String code = intent.getStringExtra(DATA);
                // if (code != null) {
                //     events.success(code);
                // }
                Map<String, String> data_map = new HashMap<>();
                if (!DATA_MAP.isEmpty()) {
                    String qr_data = intent.getStringExtra(DATA_MAP.get("qr_data_tag"));
                    String image_data = intent.getStringExtra(DATA_MAP.get("image_data_tag"));
                    String ocr_data = intent.getStringExtra(DATA_MAP.get("ocr_data_tag"));
                    data_map.put("qr_data", qr_data);
                    data_map.put("image_data", image_data);
                    data_map.put("ocr_data", ocr_data);
                    String data_amp_str = JSONObject.toJSONString(data_map);
                    events.success(data_amp_str);
                }
            }
        };
    }

}
