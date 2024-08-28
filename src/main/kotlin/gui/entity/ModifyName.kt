
package moe.nea.firmament.gui.entity

import com.google.gson.JsonObject
import net.minecraft.entity.LivingEntity
import net.minecraft.text.Text

object ModifyName : EntityModifier {
    override fun apply(entity: LivingEntity, info: JsonObject): LivingEntity {
        entity.customName = Text.literal(info.get("name").asString)
        return entity
    }

}
