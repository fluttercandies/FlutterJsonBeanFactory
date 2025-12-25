package com.github.zhangruiyu.flutterjsonbeanfactory

import com.github.zhangruiyu.flutterjsonbeanfactory.action.dart_to_helper.node.GeneratorDartClassNodeToHelperInfo
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.lang.dart.DartFileType
import java.io.File

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class MyPluginTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        // 获取测试数据目录的绝对路径
        // 根据报错：file://C:/pro/FlutterJsonBeanFactory/src/test/testData/rename/foo_after.xml
        val testDataDir = File("src/test/testData").absolutePath

        // 显式授权访问该路径
        // testRootDisposable 确保测试结束后自动撤销权限，防止污染其他测试
        VfsRootAccess.allowRootAccess(testRootDisposable, testDataDir)
    }

    fun testDartFile() {
        val psiFile = myFixture.configureByText(
            DartFileType.INSTANCE, """import 'package:testjson/generated/json/base/json_field.dart';
import 'dart:convert';

import 'package:testjson/generated/json/map_entity.g.dart';

@JsonSerializable()
class MapEntity {
  
  late List<CodeType?> codeTypesNull;

  MapEntity();

  factory MapEntity.fromJson(Map<String, dynamic> json) =>
      ${"\$"}MapEntityFromJson(json);

  Map<String, dynamic> toJson() => ${"\$"}MapEntityToJson(this);

  @override
  String toString() {
    return jsonEncode(this);
  }
}

"""
        )
        GeneratorDartClassNodeToHelperInfo.getDartFileHelperClassGeneratorInfo(psiFile)
        println(psiFile.text)
        assertNotNull(psiFile.text.isNotEmpty())
    }

    override fun getTestDataPath() = "src/test/testData/rename"

    fun testRename() {
        myFixture.testRename("foo.xml", "foo_after.xml", "a2")
    }
}
