/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import io.github.notenoughupdates.moulconfig.common.MyResourceLocation
import io.github.notenoughupdates.moulconfig.gui.CloseEventListener
import io.github.notenoughupdates.moulconfig.gui.GuiComponentWrapper
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import io.github.notenoughupdates.moulconfig.xml.ChildCount
import io.github.notenoughupdates.moulconfig.xml.XMLContext
import io.github.notenoughupdates.moulconfig.xml.XMLGuiLoader
import io.github.notenoughupdates.moulconfig.xml.XMLUniverse
import io.github.notenoughupdates.moulconfig.xml.XSDGenerator
import java.io.File
import javax.xml.namespace.QName
import me.shedaniel.math.Color
import org.w3c.dom.Element
import net.minecraft.client.gui.screen.Screen
import moe.nea.firmament.gui.BarComponent
import moe.nea.firmament.gui.FirmButtonComponent
import moe.nea.firmament.gui.FixedComponent

object MoulConfigUtils {
    val firmUrl = "http://firmament.nea.moe/moulconfig"
    val universe = XMLUniverse.getDefaultUniverse().also { uni ->
        uni.registerMapper(java.awt.Color::class.java) {
            if (it.startsWith("#")) {
                val hexString = it.substring(1)
                val hex = hexString.toInt(16)
                if (hexString.length == 6) {
                    return@registerMapper java.awt.Color(hex)
                }
                if (hexString.length == 8) {
                    return@registerMapper java.awt.Color(hex, true)
                }
                error("Hexcolor $it needs to be exactly 6 or 8 hex digits long")
            }
            return@registerMapper java.awt.Color(it.toInt(), true)
        }
        uni.registerMapper(Color::class.java) {
            val color = uni.mapXMLObject(it, java.awt.Color::class.java)
            Color.ofRGBA(color.red, color.green, color.blue, color.alpha)
        }
        uni.registerLoader(object : XMLGuiLoader.Basic<BarComponent> {
            override fun getName(): QName {
                return QName(firmUrl, "Bar")
            }

            override fun createInstance(context: XMLContext<*>, element: Element): BarComponent {
                return BarComponent(
                    context.getPropertyFromAttribute(element, QName("progress"), Double::class.java)!!,
                    context.getPropertyFromAttribute(element, QName("total"), Double::class.java)!!,
                    context.getPropertyFromAttribute(element, QName("fillColor"), Color::class.java)!!.get(),
                    context.getPropertyFromAttribute(element, QName("emptyColor"), Color::class.java)!!.get(),
                )
            }

            override fun getChildCount(): ChildCount {
                return ChildCount.NONE
            }

            override fun getAttributeNames(): Map<String, Boolean> {
                return mapOf("progress" to true, "total" to true, "emptyColor" to true, "fillColor" to true)
            }
        })
        uni.registerLoader(object : XMLGuiLoader.Basic<FirmButtonComponent> {
            override fun getName(): QName {
                return QName(firmUrl, "Button")
            }

            override fun createInstance(context: XMLContext<*>, element: Element): FirmButtonComponent {
                return FirmButtonComponent(
                    context.getChildFragment(element),
                    context.getMethodFromAttribute(element, QName("onClick")),
                    context.getPropertyFromAttribute(element, QName("enabled"), Boolean::class.java)
                        ?: GetSetter.constant(true)
                )
            }

            override fun getChildCount(): ChildCount {
                return ChildCount.ONE
            }

            override fun getAttributeNames(): Map<String, Boolean> {
                return mapOf("onClick" to true, "enabled" to false)
            }
        })
        uni.registerLoader(object : XMLGuiLoader.Basic<FixedComponent> {
            override fun createInstance(context: XMLContext<*>, element: Element): FixedComponent {
                return FixedComponent(
                    context.getPropertyFromAttribute(element, QName("width"), Int::class.java)
                        ?: error("Requires width specified"),
                    context.getPropertyFromAttribute(element, QName("height"), Int::class.java)
                        ?: error("Requires height specified"),
                    context.getChildFragment(element)
                )
            }

            override fun getName(): QName {
                return QName(firmUrl, "Fixed")
            }

            override fun getChildCount(): ChildCount {
                return ChildCount.ONE
            }

            override fun getAttributeNames(): Map<String, Boolean> {
                return mapOf("width" to true, "height" to true)
            }
        })
    }

    fun generateXSD(
        file: File,
        namespace: String
    ) {
        val generator = XSDGenerator(universe, namespace)
        generator.writeAll()
        generator.dumpToFile(file)
    }

    @JvmStatic
    fun main(args: Array<out String>) {
        generateXSD(File("MoulConfig.xsd"), XMLUniverse.MOULCONFIG_XML_NS)
        generateXSD(File("MoulConfig.Firmament.xsd"), firmUrl)
        File("wrapper.xsd").writeText("""
<?xml version="1.0" encoding="UTF-8" ?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xs:import namespace="http://notenoughupdates.org/moulconfig" schemaLocation="MoulConfig.xsd"/>
    <xs:import namespace="http://firmament.nea.moe/moulconfig" schemaLocation="MoulConfig.Firmament.xsd"/>
</xs:schema>
        """.trimIndent())
    }

    fun loadScreen(name: String, bindTo: Any, parent: Screen?): Screen {
        return object : GuiComponentWrapper(loadGui(name, bindTo)) {
            override fun close() {
                if (context.onBeforeClose() == CloseEventListener.CloseAction.NO_OBJECTIONS_TO_CLOSE) {
                    client!!.setScreen(parent)
                }
            }
        }
    }

    fun loadGui(name: String, bindTo: Any): GuiContext {
        return GuiContext(universe.load(bindTo, MyResourceLocation("firmament", "gui/$name.xml")))
    }
}
