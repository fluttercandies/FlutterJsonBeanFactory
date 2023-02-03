package com.github.zhangruiyu.flutterjsonbeanfactory.action.with

import com.intellij.util.containers.ContainerUtil
import com.jetbrains.lang.dart.DartComponentType
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartComponent

class DartGenerateCopyHandler : BaseDartGenerateHandler() {

    override fun getTitle(): String {
        return "Generate Name"
    }

    override fun createFix(dartClass: DartClass): BaseCreateMethodsFix<*> {
        if (dartClass == null) {
            //            $$$reportNull$$$0(1);
        }

        val var10000 = DartGenerateCopyFix(dartClass)
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

        candidates.addAll(ContainerUtil.findAll(this.computeClassMembersMap(dartClass, false).values) { component ->
            DartComponentType.typeOf(component) === DartComponentType.FIELD && 0 != component.name!!.indexOf("_")
        })
    }

    override fun doAllowEmptySelection(): Boolean {
        return true
    }
}
