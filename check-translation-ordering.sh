#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
#
# SPDX-License-Identifier: GPL-3.0-or-later

set -euo pipefail
jq -S --tab < translations/en_us.json | diff translations/en_us.json -
