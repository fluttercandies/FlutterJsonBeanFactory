package com.github.zhangruiyu.flutterjsonbeanfactory.action.class_body_generate

import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartComponent

class DartGenerateToFromJsonHandler : BaseDartGenerateHandler() {

    override fun getTitle(): String {
        return "Generate Name"
    }

    override fun createFix(dartClass: DartClass): BaseCreateMethodsFix<*> {
        if (dartClass == null) {
            //            $$$reportNull$$$0(1);
        }

        val var10000 = DartGenerateToFromJsonFix(dartClass)
        if (var10000 == null) {
            //            $$$reportNull$$$0(2);
        }

        return var10000
    }

    override fun collectCandidates(dartClass: DartClass, candidates: MutableList<DartComponent>) {
        if (dartClass == null) {
            //            $$$reportNull$$$0(3);
        }

        if (candidates == null) {
            //            $$$reportNull$$$0(4);
        }

    }

    override fun doAllowEmptySelection(): Boolean {
        return true
    }
}
