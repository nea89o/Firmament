package moe.nea.firmament.util

import com.mojang.blaze3d.pipeline.RenderPipeline
import java.lang.reflect.Method
import util.render.CustomRenderPipelines
import moe.nea.firmament.util.compatloader.CompatLoader

object IrisCompat {
	private var IRIS_INSTANCE: Any? = null
	private var IRIS_ASSIGN_PIPELINE_METHOD: Method? = null
	private var IRIS_PROGRAM_BASIC: Any? = null
	private var IRIS_PROGRAM_LINES: Any? = null
	private var IRIS_PROGRAMS_TEXTURED: Any? = null
	private var IRIS_PROGRAMS_ENTITY: Any? = null

	init {
		initialize()
	}

	@CompatLoader.RequireMod("iris")
	private fun initialize() {
		try {
			val irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi")
			IRIS_INSTANCE = irisApiClass.getMethod("getInstance").invoke(null)
			val irisInstanceClass = IRIS_INSTANCE!!.javaClass

			val irisProgramEnum = Class.forName("net.irisshaders.iris.api.v0.IrisProgram")
			IRIS_PROGRAM_BASIC = java.lang.Enum.valueOf(irisProgramEnum.asSubclass(Enum::class.java), "BASIC")
			IRIS_PROGRAM_LINES = java.lang.Enum.valueOf(irisProgramEnum.asSubclass(Enum::class.java), "LINES")
			IRIS_PROGRAMS_TEXTURED = java.lang.Enum.valueOf(irisProgramEnum.asSubclass(Enum::class.java), "TEXTURED")

			IRIS_ASSIGN_PIPELINE_METHOD = irisInstanceClass.getMethod("assignPipeline", RenderPipeline::class.java, irisProgramEnum)
		} catch (exception: Exception) {
			exception.printStackTrace()
		}
	}

	fun assignPipelines() {
		assignPipeline(CustomRenderPipelines.GUI_TEXTURED_NO_DEPTH_TRIS, IRIS_PROGRAMS_TEXTURED)
		assignPipeline(CustomRenderPipelines.OMNIPRESENT_LINES, IRIS_PROGRAM_LINES)
		assignPipeline(CustomRenderPipelines.COLORED_OMNIPRESENT_QUADS, IRIS_PROGRAM_BASIC)
		assignPipeline(CustomRenderPipelines.CIRCLE_FILTER_TRANSLUCENT_GUI_TRIS, IRIS_PROGRAMS_TEXTURED)
		assignPipeline(CustomRenderPipelines.PARALLAX_CAPE_SHADER, IRIS_PROGRAMS_ENTITY)
	}

	private fun assignPipeline(pipeline: RenderPipeline, enumValue: Any?) {
		enumValue ?: return
		IRIS_ASSIGN_PIPELINE_METHOD?.let { method ->
			try {
				method.invoke(IRIS_INSTANCE, pipeline, enumValue)
			} catch (exception: Exception) {
				exception.printStackTrace()
			}
		}
	}
}
