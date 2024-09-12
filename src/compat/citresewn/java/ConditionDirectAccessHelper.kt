package moe.nea.firmament.compat.citresewn

import java.lang.invoke.MethodHandles
import java.util.function.BiPredicate
import java.util.function.Function
import shcm.shsupercm.fabric.citresewn.defaults.cit.conditions.ConditionNBT

object ConditionNBTMixin {
    class Helper<StringMatcher> {
        // TODO: make lambdametafactory work by way of modifying the actual modifiers

        val stringMatcherType = ConditionNBT::class.java.getDeclaredField("matchString").type

        val accessMatcher = run {
            val matchStringF = ConditionNBT::class.java.getDeclaredField("matchString");
            matchStringF.isAccessible = true
            val l = MethodHandles.privateLookupIn(ConditionNBT::class.java, MethodHandles.lookup())
//            val mt = MethodType.methodType(stringMatcherType, ConditionNBT::class.java)
//            val callsite = LambdaMetafactory.metafactory(
//                l, "apply",
//                MethodType.methodType(Function::class.java),
//                MethodType.methodType(java.lang.Object::class.java, java.lang.Object::class.java),
//                l.unreflectGetter(matchStringF),
//                mt
//            )
            val getter = l.unreflectGetter(matchStringF)
            Function<ConditionNBT, StringMatcher> { getter.invoke(it) as StringMatcher }
        }
        val directCaller = run {
            val matchM = stringMatcherType.getDeclaredMethod("matches", String::class.java);
            matchM.isAccessible = true
            val l = MethodHandles.privateLookupIn(ConditionNBT::class.java, MethodHandles.lookup())
//            val mt = MethodType.methodType(java.lang.Boolean.TYPE, stringMatcherType, String::class.java)
//            val callsite = LambdaMetafactory.metafactory(
//                l, "test",
//                MethodType.methodType(BiPredicate::class.java),
//                mt,
//                l.unreflect(matchM),
//                mt
//            )
            val func = l.unreflect(matchM)
            BiPredicate<StringMatcher, String> { a, b -> func.invoke(a, b) as Boolean }
        }

        fun test(condition: ConditionNBT, text: String): Boolean {
            return directCaller.test(accessMatcher.apply(condition), text) as Boolean
        }
    }

    val helper = Helper<Any>()

    @JvmStatic
    fun invokeDirectConditionNBTStringMatch(
        nbt: ConditionNBT,
        text: String,
    ): Boolean {
        return helper.test(nbt, text)
    }
}
