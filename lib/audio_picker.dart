import 'dart:async';

import 'package:flutter/services.dart';

class AudioPicker {
  static const MethodChannel _channel = const MethodChannel('audio_picker');

  static Future<String> get platformVersion async {
    final String absolutePath = await _channel.invokeMethod('pick_audio');
    return absolutePath;
  }
}
