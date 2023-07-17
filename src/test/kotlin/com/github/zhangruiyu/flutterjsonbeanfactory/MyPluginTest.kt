package com.github.zhangruiyu.flutterjsonbeanfactory

import com.github.zhangruiyu.flutterjsonbeanfactory.action.dart_to_helper.node.GeneratorDartClassNodeToHelperInfo
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.PsiErrorElementUtil
import com.jetbrains.lang.dart.DartFileType

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class MyPluginTest : BasePlatformTestCase() {

    fun testDartFile() {
        val psiFile = myFixture.configureByText(
            DartFileType.INSTANCE, """import 'package:testjson/generated/json/base/json_field.dart';
import 'dart:convert';

import 'package:testjson/generated/json/map_entity.g.dart';

@JsonSerializable()
class MapEntity {
  
  String? titleMap2;

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
