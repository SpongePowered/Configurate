private val prefix = "configurate"

rootProject.name = "$prefix-parent"

listOf("core", "tool", "ext-kotlin", "bom", "examples",
        "gson", "hocon", "jackson", "xml", "yaml").forEach {
    include("$prefix-$it")
}

