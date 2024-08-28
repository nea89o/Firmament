
package moe.nea.firmament.gui.entity

import com.google.gson.JsonObject
import net.minecraft.entity.LivingEntity

object ModifyInvisible : EntityModifier {
    override fun apply(entity: LivingEntity, info: JsonObject): LivingEntity {
        entity.isInvisible = info.get("invisible")?.asBoolean ?: true
        return entity
    }

}
