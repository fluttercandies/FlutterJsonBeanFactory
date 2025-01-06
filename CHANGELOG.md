<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# FlutterJsonBeanFactory Changelog


## 5.2.3

### Changed

- late field is more intelligent

## 5.2.2

### Changed

- double quotation marks are changed to single quotation marks

## 5.2.1

### Changed

- fix [issues/167](https://github.com/fluttercandies/FlutterJsonBeanFactory/issues/167)


## 5.2.0

### Changed

- update gradle and kotlin
- fix idea version low version problem

## 5.1.7

### Changed

- fixed conflicts with freezed

## 5.1.6

### Changed

- Fix issues-161, map type conversion issue

## 5.1.5

- support nested key,such as: @JSONField(name:"login.user.name")

## 5.1.3

### Changed

- fix idea 2023.3.2 checkbox not fond exception

## 5.1.2

### Changed

- After adding analysis, the type error problem is resolved
- add copyWith annotation
- pr: https://github.com/fluttercandies/FlutterJsonBeanFactory/pull/151
- When there is no field in the class, the copyWith generation error occurs
- Support default value(list)
- Fixed an issue where hot reload would not refresh convertFuncMap after adding the model class
- copyWith private field bugfix

## 5.0.3

### Changed

- copyWith move to .g.dart

## 5.0.0

### Changed

- supper map list<map<*,*>>
- custom generated path

## 4.5.5

### Changed

- fix [issues/142](https://github.com/fluttercandies/FlutterJsonBeanFactory/issues/142)

## 4.5.3

### Changed

- asObj bugfix

## 4.5.0

### Changed

- copyWith Optional
- toJson fromJson can be generated quickly

## 4.4.9

### Changed

- add copyWith method
- enum parse support

## 4.4.6

### Changed

- asT null bugfix

## 4.5.2

### Changed

- Support default value(string,int,bool),😭next version will support map,list

## 4.4.1

### Changed

- Support map and set-

## 4.3.6

### Changed

- Faster generation

## 4.3.5

### Changed

- .g.art does not generate problem fixes

## 4.3.4

### Changed

- remove the if judgment to get generic instances and use Map instead

### Added

- add format json button

## 4.3.3

### Fixed

- dynamic type optimization

## 4.3.2

### Fixed

- A single file error does not affect other generated and show error file

## 4.3.0

### Fixed

- fix static final will not be generated

## 4.2.7

### Added

- Make your code more formal

## 4.2.5

### Added

- When going to int,double is also available

## 4.2.3

### Added

- new map modified to <String,dynamic>{}

## 4.2.2

### Added

- break changer,only support null safety
