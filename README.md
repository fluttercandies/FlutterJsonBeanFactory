# FlutterJsonBeanFactory

Hi,Welcome to come to see me!
What I do is generate dart beans based on json, as well as generics parameters and json build instances

Language: English | [中文(qq群963752388)](https://juejin.cn/post/7030739002969817118/)

打扰:我想找份flutter开发的工作,请问有没有大佬可以帮忙推荐一下,谢谢了,我的邮箱:157418979@qq.com,安卓ios都会撸,不接受做马甲包的工作
打扰:顺便接flutter外包


### Easy Use 插件交流群qq(963752388)

![image](.github/beantojson_factory.gif)

## Known issue

- If "No classes that inherit JsonConvert were found" is displayed, delete the ". Idea "directory under the project and
  click" invalidate Caches"in your (Andorid Studio or IDEA) button to restart the IDE

## Template ToDo list

- [x] Support for instantiation through generics
- [x] Support customized JSON parsing
- [x] The supported types are: int double String datetime dynamic var, and List of the above types
- [x] Two (and more)-dimensional array is supported (v4.5.6~)
- [x] Support custom generated path

<!-- Plugin description -->

### Usage

* 打扰:我想找份flutter开发的工作,请问有没有大佬可以帮忙推荐一下,谢谢了,我的邮箱:157418979@qq.com
* <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "
  FlutterJsonBeanFactory"</kbd> >
  <kbd>Install Plugin</kbd>
* Restart your Develop tools
* Modify in the YAML file of the Flutter project

* Press shortcut key `alt ` + `j` for mac , right click on package -> `New`->`Dart bean clas file from JSON`And Then you
  will know how to use
* If you change the fields in the class, just press the shortcut alt + j to regenerate the tojson and fromjson methods.
  The generated method regenerates all helper classes and JsonConvert classes (the same as the shortcut alt + j) each
  time an entity file is created in the generated/json directory.
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
      } else {
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

### 开源不易，觉得有用的话可以请作者喝杯冰可乐🥤

<img src="https://github.com/fluttercandies/FlutterJsonBeanFactory/blob/master/wechat_pay.png" width = "300" height = "300" alt="打赏"/>

### 赞助列表(非常非常的感谢,仅记录此插件收到的打赏,承诺将收到的所有赞助用于购买可乐和赞助其他开源作者)

<table>
  <thead>
    <tr>
      <th style="width: 180px;">
          <sub>名称</sub><br>
      </th>
      <th style="width: 180px;">
          <sub>金额</sub><br>
      </th>
      <th  style="width: 180px;">
          <sub>时间</sub><br>
      </th>
    </tr>
    <tr>
      <th style="width: 180px;">
          <sub>微信:大熊猫🐱</sub><br>
      </th>
      <th style="width: 180px;">
          <sub>20元</sub><br>
      </th>
      <th  style="width: 180px;">
          <sub>2024年4月3日</sub><br>
      </th>
    <tr>
    <tr>
      <th style="width: 180px;">
          <sub>qq:sunny</sub><br>
      </th>
      <th style="width: 180px;">
          <sub>10元</sub><br>
      </th>
      <th  style="width: 180px;">
          <sub>2023年12月21日</sub><br>
      </th>
    <tr>
    <tr>
      <th style="width: 180px;">
          <sub>微信:未知</sub><br>
      </th>
      <th style="width: 180px;">
          <sub>10元</sub><br>
      </th>
      <th  style="width: 180px;">
          <sub>2023年11月17日</sub><br>
      </th>
    <tr>
      <th style="width: 180px;">
          <sub>QQ:郭嘉</sub><br>
      </th>
      <th style="width: 180px;">
          <sub>10元</sub>
      </th>
      <th style="width: 180px;">
          <sub>2023年09月12日</sub>
      </th>
    </tr>
    <tr>
      <th style="width: 180px;">
          <sub>QQ:初一</sub><br>
      </th>
      <th style="width: 180px;">
          <sub>100元</sub>
      </th>
      <th style="width: 180px;">
          <sub>2023年08月15日</sub>
      </th>
    </tr>
    <tr>
      <th style="width: 180px;">
          <sub>Github:cr1992</sub><br>
      </th>
       <th style="width: 180px;">
          <sub>6.66元</sub>
      </th>
       <th style="width: 180px;">
          <sub>2023年08月4日</sub>
      </th>
    </tr>
    <tr>
     <th style="width: 180px;">
          <sub>QQ:余军</sub><br>
      </th>
     <th style="width: 180px;">
          <sub>200元</sub>
      </th>
     <th style="width: 180px;">
          <sub>2022年12月</sub>
      </th>
    </tr>

  </thead>
</table>

### Find me useful ? :heart:

* Support me by clicking the :star: button on the upper right of this page. :v:
* Spread to others to let more people have a better develope expierience :heart:

---
Thanks to [JetBrains](https://www.jetbrains.com/?from=fluttercandies) for allocating free open-source licenses for IDEs
such as [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=fluttercandies).

[<img src=".github/jetbrains-variant.png" width="200"/>](https://www.jetbrains.com/?from=fluttercandies)