
# FlutterJsonBeanFactory

Hi,Welcome to come to see me!
What I do is generate dart beans based on json, as well as generics parameters and json build instances


### Easy Use
![image](https://github.com/zhangruiyu/FlutterJsonBeanFactory/blob/master/beantojson_factory.gif)

## Template ToDo list
- [ ] Support for instantiation through generics
- [ ] Support customized JSON parsing
- [ ] The supported types are: int double String datetime dynamic var, and List of the above types
- [x] Two (and more)-dimensional array is not supported

<!-- Plugin description -->
### Usage
* <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "FlutterJsonBeanFactory"</kbd> >
  <kbd>Install Plugin</kbd>
* Restart your Develop tools
* Press shortcut key `alt ` + `j` for mac  , right click on package -> `New`->`Dart bean clas file from JSON`　And Then you will know how to use
* If you change the fields in the class, just press the shortcut alt + j to regenerate the tojson and fromjson methods. The generated method regenerates all helper classes and JsonConvert classes (the same as the shortcut alt + j) each time an entity file is created in the generated/json directory.
* If you need generic conversions in your network requests, use the jsonconvert.fromjsonast method directly.
* If you don't want to use the FlutterJsonBeanFactory in your project, you can add flutter-json: enable: false to the pubspec.yaml file
* If no helper files are generated, you can delete the .idea directory and restart your idea
  ![image](https://github.com/zhangruiyu/FlutterJsonBeanFactory/blob/master/filter_json_config.png)
* 如果你有任何问题,添加QQGroup(963752388)随时提问 [中文移步](https://www.jianshu.com/p/14cbcbaa74b7)
<!-- Plugin description end -->

### Find me useful ? :heart:
* Support me by clicking the :star: button on the upper right of this page. :v:
* Spread to others to let more people have a better develope expierience :heart:
---
Thanks to [JetBrains](https://www.jetbrains.com/?from=fluttercandies) for allocating free open-source licenses for IDEs
such as [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=fluttercandies).

[<img src=".github/jetbrains-variant.png" width="200"/>](https://www.jetbrains.com/?from=fluttercandies)