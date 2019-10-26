import 'package:audio_picker/audio_picker.dart';
import 'package:audioplayers/audioplayers.dart';
import 'package:flutter/material.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _absolutePathOfAudio;
  AudioPlayer audioPlayer;

  @override
  void initState() {
    super.initState();
    audioPlayer = AudioPlayer();
  }

  void openAudioPicker() async {
    var path = await AudioPicker.platformVersion;

    setState(() {
      _absolutePathOfAudio = path;
    });
  }

  void playMusic() async {
    await audioPlayer.play(_absolutePathOfAudio, isLocal: true);
  }

  void stopMusic() async {
    await audioPlayer.stop();
  }

  void resumeMusic() async {
    await audioPlayer.resume(); // quickly plays the sound, will not release
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Audio picker example'),
        ),
        body: Column(
          mainAxisSize: MainAxisSize.max,
          children: <Widget>[
            Center(
                child: FlatButton(
              child: Text("Select an audio"),
              onPressed: () {
                openAudioPicker();
              },
            )),
            _absolutePathOfAudio == null
                ? Container()
                : Text("Absolute path : $_absolutePathOfAudio"),
            FlatButton(
              child: Text("Play"),
              onPressed: playMusic,
            ),
            FlatButton(
              child: Text("Stop"),
              onPressed: stopMusic,
            )
          ],
        ),
      ),
    );
  }
}
