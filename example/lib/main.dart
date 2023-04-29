import 'package:audio_picker/audio_picker.dart';
import 'package:audioplayers/audioplayers.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _absolutePathOfAudio;
  AudioPlayer audioPlayer;
  final _scaffoldKey = GlobalKey<ScaffoldState>();
  final navigatorKey = GlobalKey<NavigatorState>();

  @override
  void initState() {
    super.initState();
    audioPlayer = AudioPlayer();
  }

  void showLoading() {
    showDialog(
      context: navigatorKey.currentState.overlay.context,
      barrierDismissible: false,
      builder: (BuildContext context) {
        return Dialog(
          child: new Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              new CircularProgressIndicator(),
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: new Text('Loading'),
              ),
            ],
          ),
        );
      },
    );
  }

  void dismissLoading() {
    Navigator.pop(navigatorKey.currentState.overlay.context);
  }

  void openAudioPicker() async {
    showLoading();
    var path = await AudioPicker.pickAudio();
    dismissLoading();
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
      navigatorKey: navigatorKey,
      home: Scaffold(
        key: _scaffoldKey,
        appBar: AppBar(
          backgroundColor: Colors.black,
          title: const Text('Audio picker example'),
        ),
        body: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: <Widget>[
              TextButton(
                style: ButtonStyle(
                    backgroundColor: MaterialStateProperty.all<Color>(
                  Colors.orange,
                )),
                child: Text(
                  'Select an audio',
                  style: TextStyle(color: Colors.white),
                ),
                onPressed: () {
                  openAudioPicker();
                },
              ),
              _absolutePathOfAudio == null
                  ? Container()
                  : Text(
                      'Absolute path',
                      style: TextStyle(fontWeight: FontWeight.w700),
                    ),
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: GestureDetector(
                  onTap: () {
                    Clipboard.setData(
                        new ClipboardData(text: _absolutePathOfAudio));
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('Copied path !')),
                    );
                  },
                  child: _absolutePathOfAudio == null
                      ? Container()
                      : Text(_absolutePathOfAudio),
                ),
              ),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: <Widget>[
                  _absolutePathOfAudio == null
                      ? Container()
                      : TextButton(
                          style: ButtonStyle(
                            backgroundColor: MaterialStateProperty.all<Color>(
                              Colors.green[400],
                            ),
                          ),
                          child: Text(
                            'Play',
                            style: TextStyle(color: Colors.white),
                          ),
                          onPressed: playMusic,
                        ),
                  _absolutePathOfAudio == null
                      ? Container()
                      : TextButton(
                          style: ButtonStyle(
                            backgroundColor: MaterialStateProperty.all<Color>(
                              Colors.red[400],
                            ),
                          ),
                          child: Text(
                            'Stop',
                            style: TextStyle(color: Colors.white),
                          ),
                          onPressed: stopMusic,
                        )
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
