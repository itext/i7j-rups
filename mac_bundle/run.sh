#!/usr/bin/env bash

HERE=${BASH_SOURCE%/*}

"$HERE/custom-runtime/bin/java" -jar "$HERE/app.jar" "$@"