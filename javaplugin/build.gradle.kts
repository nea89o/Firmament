plugins {
    java
}
dependencies {
    implementation("net.fabricmc:stitch:0.6.2")
}
tasks.withType(JavaCompile::class) {
    val module = "ALL-UNNAMED"
    options.compilerArgs.addAll(listOf(
        "--add-exports=jdk.compiler/com.sun.tools.javac.util=$module",
        "--add-exports=jdk.compiler/com.sun.tools.javac.comp=$module",
        "--add-exports=jdk.compiler/com.sun.tools.javac.tree=$module",
        "--add-exports=jdk.compiler/com.sun.tools.javac.api=$module",
        "--add-exports=jdk.compiler/com.sun.tools.javac.code=$module",
    ))
}
