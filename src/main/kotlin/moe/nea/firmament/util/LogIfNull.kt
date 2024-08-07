
package moe.nea.firmament.util


fun runNull(block: () -> Unit): Nothing? {
    block()
    return null
}
