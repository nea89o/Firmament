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
    reuse
    pre-commit
    glfw
    jdk21
    libGL
    wayland
    flite
    jack2
    openal
    pulseaudio
    pipewire
    glibc
  ];
  shellHook = ''
    export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:${pkgs.glfw}/lib"
    export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:${pkgs.pipewire}/lib"
    export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:${pkgs.pulseaudio}/lib"
    export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:${pkgs.jack2}/lib"
    export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:${pkgs.openal}/lib"
    export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:${pkgs.libGL}/lib"
    export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:${pkgs.wayland}/lib"
    export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:${pkgs.flite}/lib"
    export JAVA_HOME=${pkgs.jdk21}
    apply() {
        echo "LD_LIBRARY_PATH=$LD_LIBRARY_PATH" >.env
        echo org.lwjgl.glfw.libname=${pkgs.glfw}/lib/libglfw.so >.properties
        echo "jna.library.path=$LD_LIBRARY_PATH" >>.properties
        echo "java.library.path=$LD_LIBRARY_PATH" >>.properties
    }

  '';
}
