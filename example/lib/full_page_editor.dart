import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:tun_editor/iconfont.dart';
import 'package:tun_editor/models/documents/document.dart';
import 'package:tun_editor/tun_editor.dart';
import 'package:tun_editor/tun_editor_toolbar.dart';
import 'package:tun_editor/controller.dart';

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

  FocusNode _titleFocusNode = FocusNode();
  FocusNode _editorFocusNode = FocusNode();

  @override
  void initState() {
    super.initState();
  
    _loadDocument();
    // _editorFocusNode.addListener(() {
    //   if (_editorFocusNode.hasFocus) {
    //     _titleFocusNode.unfocus();
    //     _editorFocusNode.requestFocus();
    //   }
    // });
  }

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return const Scaffold(body: Center(child: Text('Loading...')));
    }

    return Scaffold(
      appBar: AppBar(
        title: GestureDetector(
          child: Text("Editor"),
          onTap: () {
            // _controller.insertMention('1', 'Jeffrey Wu');
            // if (focusNode.hasFocus) {
            //   focusNode.unfocus();
            // } else {
            //   focusNode.requestFocus();
            // }
            // _controller.insertImage('https://avatars0.githubusercontent.com/u/1758864?s=460&v=4');
            // _controller.formatText(0, 2, Attribute.h1);
            // _controller.insert(2, 'Bye Bye');
            //   _controller.insert(_controller.selection.baseOffset, "🛹");
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
              TextField(
                focusNode: _titleFocusNode,
                textInputAction: TextInputAction.next,
              ),
              Expanded(
                child: TunEditor(
                  controller: _controller,
                  padding: EdgeInsets.symmetric(
                    vertical: 12,
                    horizontal: 15,
                  ),
                  placeholder: "Hello World!",
                  focusNode: _editorFocusNode,
                  autoFocus: false,
                  readOnly: false,

                  onMentionClick: (String id, String text) {
                    debugPrint('metion click $id, $text');
                  },
                  onLinkClick: (String url) {
                    debugPrint('link click $url');
                  },
                  onFocusChange: (bool hasFocus) {
                    // if (hasFocus) {
                    //   _titleFocusNode.nextFocus();
                    // }
                    // _titleFocusNode.unfocus();
                  },
                ),
              ),
              TunEditorToolbar(
                controller: _controller,
                showingAt: false,
                showingImage: false,
                showingEmoji: false,
                onAtChange: (bool isShow) {
                  debugPrint('show at subtoolbar change: $isShow');
                },
                onImageChange: (bool isShow) {
                  debugPrint('show image subtoolbar change: $isShow');
                },
                onEmojiChange: (bool isShow) {
                  debugPrint('show emoji sub toolbar change: $isShow');
                },
                onSend: () {
                  debugPrint('send click');
                },

                // menu: [
                //   ToolbarMenu.textType,
                //   ToolbarMenu.textTypeHeadline1,
                //   ToolbarMenu.textTypeHeadline2,
                //   ToolbarMenu.textTypeHeadline3,

                //   ToolbarMenu.textStyle,
                //   ToolbarMenu.textStyleBold,
                //   ToolbarMenu.textStyleItalic,

                //   ToolbarMenu.link,
                // ],
               children: [
                 Spacer(),

                 // Send button.
                 GestureDetector(
                   onTap: () {},
                   child: Container(
                     width: 48,
                     height: 36,
                     decoration: BoxDecoration(
                       color: Color(0x268F959E),
                       borderRadius: BorderRadius.circular(18),
                     ),
                     child: Icon(
                       IconFont.send,
                       size: 24,
                       color: Color(0xA6363940),
                     ),
                   ),
                 ),
               ],
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
      // final delta1 = json.encode(event.item1.toJson());
      final delta2 = json.encode(event.item2.toJson());
      debugPrint('event:  $delta2');

      final doc = json.encode(_controller.document.toDelta().toJson());
      debugPrint('document: $doc');
    });
    setState(() {
      isLoading = false;
    });
  }

}
