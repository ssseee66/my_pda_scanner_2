import 'dart:async';

import 'package:flutter/services.dart';

class MyPdaScanner {
  static const MethodChannel _channel =
  const MethodChannel('my_pda_scanner');

  static Future<String?> Init() async {
    final String? code = await _channel.invokeMethod('init');
    return code;
  }
}
