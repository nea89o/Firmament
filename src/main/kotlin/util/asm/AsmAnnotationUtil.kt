package moe.nea.firmament.util.asm

import com.google.common.base.Defaults
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode

object AsmAnnotationUtil {
	class AnnotationProxy(
		val originalType: Class<out Annotation>,
		val annotationNode: AnnotationNode,
	) : InvocationHandler {
		val offsets = annotationNode.values.withIndex()
			.chunked(2)
			.map { it.first() }
			.associate { (idx, value) -> value as String to idx + 1 }

		fun nestArrayType(depth: Int, comp: Class<*>): Class<*> =
			if (depth == 0) comp
			else java.lang.reflect.Array.newInstance(nestArrayType(depth - 1, comp), 0).javaClass

		fun unmap(
			value: Any?,
			comp: Class<*>,
			depth: Int,
		): Any? {
			value ?: return null
			if (depth > 0)
				return ((value as List<Any>)
					.map { unmap(it, comp, depth - 1) } as java.util.List<Any>)
					.toArray(java.lang.reflect.Array.newInstance(nestArrayType(depth - 1, comp), 0) as Array<*>)
			if (comp.isEnum) {
				comp as Class<out Enum<*>>
				when (value) {
					is String -> return java.lang.Enum.valueOf(comp, value)
					is List<*> -> return java.lang.Enum.valueOf(comp, value[1] as String)
					else -> error("Unknown enum variant $value for $comp")
				}
			}
			when (value) {
				is Type -> return Class.forName(value.className)
				is AnnotationNode -> return createProxy(comp as Class<out Annotation>, value)
				is String, is Boolean, is Byte, is Double, is Int, is Float, is Long, is Short, is Char -> return value
			}
			error("Unknown enum variant $value for $comp")
		}

		fun defaultFor(fullType: Class<*>): Any? {
			if (fullType.isArray) return java.lang.reflect.Array.newInstance(fullType.componentType, 0)
			if (fullType.isPrimitive) {
				return Defaults.defaultValue(fullType)
			}
			if (fullType == String::class.java)
				return ""
			return null
		}

		override fun invoke(
			proxy: Any,
			method: Method,
			args: Array<out Any?>?
		): Any? {
			val name = method.name
			val ret = method.returnType
			val retU = generateSequence(ret) { if (it.isArray) it.componentType else null }
				.toList()
			val arrayDepth = retU.size - 1
			val componentType = retU.last()

			val off = offsets[name]
			if (off == null) {
				return defaultFor(ret)
			}
			return unmap(annotationNode.values[off], componentType, arrayDepth)
		}
	}

	fun <T : Annotation> createProxy(
		annotationClass: Class<T>,
		annotationNode: AnnotationNode
	): T {
		require(Type.getType(annotationClass) == Type.getType(annotationNode.desc))
		return Proxy.newProxyInstance(javaClass.classLoader,
		                              arrayOf(annotationClass),
		                              AnnotationProxy(annotationClass, annotationNode)) as T
	}
}
