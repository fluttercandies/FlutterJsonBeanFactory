package com.github.zhangruiyu.flutterjsonbeanfactory.action.class_body_generate

import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateAction
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler


class DartGenerateToFromJsonAction : BaseDartGenerateAction() {

    override fun getGenerateHandler(): BaseDartGenerateHandler {
        return DartGenerateToFromJsonHandler()
    }
}


