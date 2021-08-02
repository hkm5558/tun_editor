import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:tun_editor/models/documents/document.dart';
import 'package:tun_editor/tun_editor.dart';
import 'package:tun_editor/tun_editor_controller.dart';
import 'package:tun_editor/tun_editor_toolbar.dart';

class FullPageEditor extends StatefulWidget {

  const FullPageEditor({
    Key? key,
  }) : super(key: key);

  @override
  State<StatefulWidget> createState() => FullPageEditorState();

}

class FullPageEditorState extends State<FullPageEditor> {

  bool isLoading = true;
  late TunEditorController _controller;

  String _previewText = "";

  FocusNode focusNode = FocusNode();

  @override
  void initState() {
    super.initState();
  
    _loadDocument();
  }

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return const Scaffold(body: Center(child: Text('Loading...')));
    }

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: GestureDetector(
          child: Text("Editor"),
          onTap: () {
            if (focusNode.hasFocus) {
              focusNode.unfocus();
            } else {
              focusNode.requestFocus();
            }
            // _controller.formatText(0, 2, Attribute.h1);
            // _controller.insert(2, 'Bye Bye');
            // _controller.replaceText(6, 5, 'Jeffrey Wu', null);
          },
        ),
      ),
      body: SafeArea(
        child: Container(
          width: double.infinity,
          height: double.infinity,
          alignment: Alignment.center,
          child: Column(
            children: [
              Expanded(
                child: TunEditor(
                  controller: _controller,
                  placeholder: "Hello World!",
                  focusNode: focusNode,
                  autoFocus: true,
                  readOnly: false,
                ),
              ),
              // SizedBox(
              //   height: 50,
              //   child: SingleChildScrollView(
              //     child: Text(
              //       _previewText,
              //     ),
              //   ),
              // ),
              TunEditorToolbar(
                controller: _controller,
                onAtClick: () {
                },
                onImageClick: () {
                  _controller.insertImage(
                    'https://avatars0.githubusercontent.com/u/1758864?s=460&v=4',
                    'test iamge',
                  );
                },
                onEmojiClick: () {
                },
              ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    _controller.dispose();
  
    super.dispose();
  }

  Future<void> _loadDocument() async {
    final result = await rootBundle.loadString('assets/sample_data.json');
    final doc = Document.fromJson(jsonDecode(result));

    _controller = TunEditorController(
        document: doc,
        selection: TextSelection.collapsed(offset: 0),
    );
    _controller.document.changes.listen((event) {
      final delta1 = json.encode(event.item1.toJson());
      final delta2 = json.encode(event.item2.toJson());
      debugPrint('event: $delta1 - $delta2');

      final doc = json.encode(_controller.document.toDelta().toJson());
      debugPrint('document: $doc');

      setState(() {
        _previewText = doc;
      });
    });
    focusNode.addListener(() {
      debugPrint('focus node listener: ${focusNode.hasFocus}');
    });
    setState(() {
      isLoading = false;
    });
  }

}
