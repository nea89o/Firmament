

package moe.nea.firmament.util


fun <T> errorBoundary(block: () -> T): T? {
    // TODO: implement a proper error boundary here to avoid crashing minecraft code
    return block()
}

