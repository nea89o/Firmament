package moe.nea.firmament.gui

import io.github.cottonmc.cotton.gui.widget.WPanel
import io.github.cottonmc.cotton.gui.widget.WWidget

class WFixedPanel() : WPanel() {
    var child: WWidget
        set(value) {
            children.clear()
            setSize(0, 0)
            value.parent = this
            children.add(value)
        }
        get() = children.single()

    constructor(child: WWidget) : this() {
        this.child = child
    }

    override fun layout() {
        setSize(0, 0)
        super.layout()
    }

    override fun canResize(): Boolean = false
}
