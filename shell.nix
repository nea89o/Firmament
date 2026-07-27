# SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
#
# SPDX-License-Identifier: GPL-3.0-or-later
{
  pkgs ? import (builtins.fetchTarball {
    sha256 = "01vhgw5s0sw5makh0i6mhdbz1ilfyhl8mymdh780x37v3mpyqqr6";
    url = "https://releases.nixos.org/nixos/unstable/nixos-26.11pre1042126.624af665418d/nixexprs.tar.xz";
  }) { },
}:
pkgs.mkShell {
  buildInputs =
    with pkgs;
    [
      argbash
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
    ]
    ++ ([
      # For web
      python3
      node-gyp
      node-pre-gyp
      nodejs_22
      corepack
      pixman
      cairo.dev
      pango.dev
      glib.dev
      harfbuzz.dev
      gnumake
      fontconfig.dev
      clang
      freetype.dev
      libpng.dev
      pkg-config
    ]);
  LD_LIBRARY_PATH = pkgs.lib.strings.makeLibraryPath (
    with pkgs;
    [
      glfw
      pipewire
      jack2
      pulseaudio
      openal
      libGL
      wayland
      flite
    ]
  );
  JAVA_HOME = "${pkgs.jdk25}";
  shellHook = ''
    apply() {
        echo "LD_LIBRARY_PATH=$LD_LIBRARY_PATH" >.env
        echo org.lwjgl.glfw.libname=${pkgs.glfw}/lib/libglfw.so >.properties
        echo "jna.library.path=$LD_LIBRARY_PATH" >>.properties
        echo "java.library.path=$LD_LIBRARY_PATH" >>.properties
    }
  '';
}
