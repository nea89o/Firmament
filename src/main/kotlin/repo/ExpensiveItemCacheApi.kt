package moe.nea.firmament.repo

/**
 * Marker for functions that could potentially invoke DFU. Please do not call on a lot of objects at once, or try to make sure the item is cached and fall back to a more gentle function call using [SBItemStack.isWarm] and similar functions.
 */
@RequiresOptIn
@Retention(AnnotationRetention.BINARY)
annotation class ExpensiveItemCacheApi
