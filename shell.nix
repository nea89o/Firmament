# SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
#
# SPDX-License-Identifier: GPL-3.0-or-later
{pkgs ? import <nixpkgs> {}}:
pkgs.mkShell {
  buildInputs = with pkgs; [
    bash
    gh
    git
    xdg-utils
    temurin-bin-17
    reuse
    pre-commit
    glfw
    libGL
  ];
  shellHook = ''
    export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:${pkgs.glfw}/lib"
    export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:${pkgs.libGL}/lib"
  '';
}
