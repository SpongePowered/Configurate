private val prefix = "configurate"

rootProject.name = "$prefix-parent"

enableFeaturePreview("ONE_LOCKFILE_PER_PROJECT")

// core
listOf("core", "tool", "bom", "examples").forEach {
    include(":$it")
    findProject(":$it")?.name = "$prefix-$it"
}

// formats
listOf("gson", "hocon", "jackson", "xml", "yaml").forEach {
    include(":format:$it")
    findProject(":format:$it")?.name = "$prefix-$it"
}

// extras
listOf("kotlin", "guice", "dfu2", "dfu3", "dfu4").forEach {
    include(":extra:$it")
    findProject(":extra:$it")?.name = "$prefix-extra-$it"
}
