==========
thirdparty
==========

This is a section of the project including various third-party libraries that
are distributed as part of Configurate. While it is preferable to use published
libraries, this allows using projects that are forked in order to patch bugs or
receive new features ahead of upstream, without these changes being visible to
downstream users.

snakeyaml
---------

A fork of snakeyaml for fixes to comment handling that have not yet made it into
an upstream release.

typesafe-config
---------------

Modifications and enhancements to typesafe-config that are useful for
Configurate's use case, but that upstream does not want to adopt due to the
wider scope of their configuration loading API.
