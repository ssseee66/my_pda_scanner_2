import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class MyPdaScannerUtil {
  MyPdaScannerUtil._();

  factory MyPdaScannerUtil() => _instance;
  static final MyPdaScannerUtil _instance = MyPdaScannerUtil._();
  bool showLog = true;

  EventChannel eventChannel = EventChannel('my_pda_channel');
  MethodChannel flutterChannel = MethodChannel("flutter_to_android");

  void printLog(dynamic log) {
    if (showLog) {
      debugPrint('商米: $log');
    }
  }

  StreamSubscription? streamSubscription;
  Stream? stream;

  Stream start() {
    stream ??= eventChannel.receiveBroadcastStream();
    return stream!;
  }

  /// 监听扫码数据
  void listen(ValueChanged<String> codeHandle) {
    streamSubscription = start().listen((event) {
      if (event != null) {
        printLog('扫描到数据$event');
        codeHandle.call(event.toString());
      }
    });
  }

  void sendMessageToAndroid(Map<String, String> data_map) async {
    try {
      await flutterChannel.invokeMapMethod("sendMessage", {
        "pda_action": data_map['pda_action'],
        "qr_data_tag": data_map['qr_data_tag'],
        "image_data_tag": data_map['image_data_tag'],
        "ocr_data_tag": data_map['ocr_data_tag'],
      });
    } catch (e) {
      print('Error: $e');
    }
  }

  /// 关闭监听
  void cancel() {
    streamSubscription?.cancel();
  }
}
