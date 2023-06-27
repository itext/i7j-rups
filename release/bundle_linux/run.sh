#!/usr/bin/env bash

HERE=${BASH_SOURCE%/*}

"$HERE/jre/bin/java" -jar "$HERE/app.jar" "$@"