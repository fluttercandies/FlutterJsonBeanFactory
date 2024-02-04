<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Changelog

## [5.1.5]

### Added
- support nested key,such as: @JSONField(name:"login.user.name")

# Changelog

## [5.1.3]

### Bugfix
- fix idea 2023.3.2 checkbox not fond exception

# Changelog

## [5.1.2]

### Bugfix
- After adding analysis, the type error problem is resolved

### Added
- add copyWith annotation

## [5.1.0]

### Added
- pr: https://github.com/fluttercandies/FlutterJsonBeanFactory/pull/151

## [5.0.9]

### Bugfix
- When there is no field in the class, the copyWith generation error occurs

## [5.0.8]

### Added
- Support default value(list)
- Fixed an issue where hot reload would not refresh convertFuncMap after adding the model class 

### Bugfix
- copyWith private field bugfix

## [5.0.3]

### Added
- copyWith move to .g.dart

## [5.0.0]

### Added

- supper map list<map<*,*>>
- custom generated path

## [4.5.5]

### Bugfix

- fix [issues/142](https://github.com/fluttercandies/FlutterJsonBeanFactory/issues/142)

## [4.5.3]

### Bugfix

- asObj bugfix

## [4.5.0]

### Added

- copyWith Optional
- toJson fromJson can be generated quickly

## [4.4.9]

### Added

- add copyWith method
- enum parse support

## [4.4.6]

### Added

- asT null bugfix

## [4.5.2]

### Added

- Support default value(string,int,bool),😭next version will support map,list

## [4.4.1]

### Added

- Support map and set-

## [4.3.6]

### Fixed

- Faster generation

## [4.3.5]

### Fixed

- .g.art does not generate problem fixes

## [4.3.4]

### Fixed

- remove the if judgment to get generic instances and use Map instead

### Added

- add format json button

## [4.3.3]

### Fixed

- dynamic type optimization

## [4.3.2]

### Fixed

- A single file error does not affect other generated and show error file

## [4.3.0]

### Fixed

- fix static final will not be generated

## [4.2.7]

### Added

- Make your code more formal

## [4.2.5]

### Added

- When going to int,double is also available

## [4.2.3]

### Added

- new map modified to <String,dynamic>{}

## [4.2.2]

### Added

- break changer,only support null safety