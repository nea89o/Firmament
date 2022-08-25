package moe.nea.notenoughupdates.dbus

import org.freedesktop.dbus.annotations.DBusInterfaceName
import org.freedesktop.dbus.interfaces.DBusInterface

@DBusInterfaceName("moe.nea.NotEnoughUpdates")
interface NEUDbusInterface : DBusInterface {
    fun sayHello(): String
    fun getCurrentRepoCommit(): String
    fun requestRepoReDownload()
}
