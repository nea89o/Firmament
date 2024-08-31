package moe.nea.firmament.compat.configured

import com.mrcrayfish.configured.api.IConfigValue
import net.minecraft.text.Text
import moe.nea.firmament.gui.config.ManagedOption

class ConfigValue<T: Any>(val option: ManagedOption<T>) : IConfigValue<T> {
    var value = option.get()
    var initialValue = option.get()

    override fun get(): T {
        return value
    }

    override fun set(p0: T) {
        this.value = p0
    }

    override fun getDefault(): T {
        return option.default()
    }

    override fun isDefault(): Boolean {
        // TODO: should this be an option in handlers?
        return option == option.default()
    }

    override fun isChanged(): Boolean {
        return value != initialValue
    }

    override fun restore() {
        this.value = option.default()
    }

    override fun getComment(): Text? {
        return null
    }

    override fun getTranslationKey(): String? {
        return option.rawLabelText
    }

    override fun getValidationHint(): Text? {
        return null
    }

    override fun getName(): String {
        return ""
    }

    override fun cleanCache() {

    }

    override fun requiresWorldRestart(): Boolean {
        return false
    }

    override fun requiresGameRestart(): Boolean {
        return false
    }

    override fun isValid(p0: T): Boolean {
        // TODO: should this be validated?
        return true
    }

    fun saveValue() {
        option.set(value)
    }
}
