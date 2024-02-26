/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import io.github.notenoughupdates.moulconfig.common.MyResourceLocation
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.xml.ChildCount
import io.github.notenoughupdates.moulconfig.xml.XMLContext
import io.github.notenoughupdates.moulconfig.xml.XMLGuiLoader
import io.github.notenoughupdates.moulconfig.xml.XMLUniverse
import javax.xml.namespace.QName
import me.shedaniel.math.Color
import org.w3c.dom.Element
import moe.nea.firmament.gui.BarComponent

object MoulConfigUtils {
    val firmUrl = "http://nea.moe/Firmament"
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
        uni.registerLoader(object : XMLGuiLoader<BarComponent> {
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
    }

    fun loadGui(name: String, bindTo: Any): GuiContext {
        return GuiContext(universe.load(bindTo, MyResourceLocation("firmament", "gui/$name.xml")))
    }
}
