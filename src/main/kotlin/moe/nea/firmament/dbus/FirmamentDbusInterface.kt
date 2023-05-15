package moe.nea.firmament.dbus

import org.freedesktop.dbus.annotations.DBusInterfaceName
import org.freedesktop.dbus.interfaces.DBusInterface

@DBusInterfaceName("moe.nea.Firmament")
interface FirmamentDbusInterface : DBusInterface {
    fun sayHello(): String
    fun getCurrentRepoCommit(): String
    fun requestRepoReDownload()
}
