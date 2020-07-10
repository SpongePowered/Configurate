private val prefix = "configurate"

rootProject.name = "$prefix-parent"

// core
listOf("core", "tool", "ext-kotlin", "bom", "examples").forEach {
    include(":$it")
    // findProject(":$it")?.name = "$prefix-$it"
}

// formats
listOf("gson", "hocon", "jackson", "xml", "yaml").forEach {
    include(":format:$it")
    // findProject(":format:$it")?.name = "$prefix-$it"
}

// extras
listOf("kotlin", "guice", "dfu2", "dfu3", "dfu4").forEach {
    include(":extra:$it")
    findProject(":extra:$it")?.name = "extra-$it"
}
