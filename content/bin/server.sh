#!/bin/sh
{{JAVA_EXEC}} -server -Xms64m -Xmx512m -classpath {{ROOT}}/lib/autohit.jar:{{ROOT}}/lib autohit.server.invoker.BasicServer {{ROOT}}/etc/default.prop server


