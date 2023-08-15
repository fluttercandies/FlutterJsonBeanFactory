
# FlutterJsonBeanFactory

Hi,Welcome to come to see me!
What I do is generate dart beans based on json, as well as generics parameters and json build instances

Language: English | [中文(qq群963752388)](https://juejin.cn/post/7030739002969817118/)

打扰:我想在北京找份flutter开发的工作,请问有没有大佬可以帮忙推荐一下,谢谢了,我的邮箱:157418979@qq.com

### Easy Use
![image](.github/beantojson_factory.gif)

## Known issue
- If "No classes that inherit JsonConvert were found" is displayed, delete the ". Idea "directory under the project and click" invalidate Caches"in your (Andorid Studio or IDEA) button to restart the IDE

## Template ToDo list
- [x] Support for instantiation through generics
- [x] Support customized JSON parsing
- [x] The supported types are: int double String datetime dynamic var, and List of the above types
- [x] Two (and more)-dimensional array is supported (v4.5.6~)
- [x] Support custom generated path

<!-- Plugin description -->
### Usage
* 打扰:我想在北京找份flutter开发的工作,请问有没有大佬可以帮忙推荐一下,谢谢了,我的邮箱:157418979@qq.com
* <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "FlutterJsonBeanFactory"</kbd> >
  <kbd>Install Plugin</kbd>
* Restart your Develop tools
* Modify in the YAML file of the Flutter project

before:
```
environment:
  sdk: ">=2.12.0 <3.0.0"
```
after modification:
```
environment:
  sdk: ">=2.15.0 <3.0.0"
```
* Press shortcut key `alt ` + `j` for mac  , right click on package -> `New`->`Dart bean clas file from JSON`　And Then you will know how to use
* If you change the fields in the class, just press the shortcut alt + j to regenerate the tojson and fromjson methods. The generated method regenerates all helper classes and JsonConvert classes (the same as the shortcut alt + j) each time an entity file is created in the generated/json directory.
* If you need generic conversions in your network requests, use the JsonConvert.fromJsonAsT<T> method directly.
* If no helper files are generated, you can delete the .idea directory and restart your idea
* You can customize the JSON parsing scheme
```dart
import 'generated/json/base/json_convert_content.dart';

class MyJsonConvert extends JsonConvert {
  T? asT<T extends Object?>(dynamic value) {
    try {
      String type = T.toString();
      if (type == "DateTime") {
        return DateFormat("dd.MM.yyyy").parse(value) as T;
      }else{
        return super.asT<T>(value);
      }
    } catch (e, stackTrace) {
      print('asT<$T> $e $stackTrace');
      return null;
    }
  }
}

Future<void> main() async {
  jsonConvert = MyJsonConvert();
  runApp(Text("OK"));
}
```

custom generated path->(pubspec.yaml)
```yaml 
flutter_json:
  generated_path: src/json/**
```

<!-- Plugin description end -->

### Find me useful ? :heart:
* Support me by clicking the :star: button on the upper right of this page. :v:
* Spread to others to let more people have a better develope expierience :heart:
---
Thanks to [JetBrains](https://www.jetbrains.com/?from=fluttercandies) for allocating free open-source licenses for IDEs
such as [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=fluttercandies).

[<img src=".github/jetbrains-variant.png" width="200"/>](https://www.jetbrains.com/?from=fluttercandies)