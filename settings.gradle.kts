private val prefix = "configurate"

rootProject.name = "$prefix-parent"

listOf("core", "tool", "ext-kotlin",
        "gson", "hocon", "json", "xml", "yaml").forEach {
    include("$prefix-$it")
}

