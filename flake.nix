# SPDX-FileCopyrightText: 2026 Linnea Gräf <nea@nea.moe>
#
# SPDX-License-Identifier: GPL-3.0-or-later
{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

    flake-utils.url = "github:numtide/flake-utils";

    treefmt-nix.url = "github:numtide/treefmt-nix";
    treefmt-nix.inputs.nixpkgs.follows = "nixpkgs";
  };
  outputs =
    inputs@{
      treefmt-nix,
      flake-utils,
      self,
      nixpkgs,
      ...
    }:
    let
      overlays = [
      ];
    in
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = import nixpkgs { inherit overlays system; };
        treefmtEval =
          (treefmt-nix.lib.evalModule pkgs {
            projectRootFile = "flake.nix";
            imports = [ ];
            programs.nixfmt.enable = true;
          }).config.build;
      in
      {
        formatter = treefmtEval.wrapper;
        checks.formatting = treefmtEval.check self;
        devShells.default = import ./shell.nix { inherit pkgs; };
      }
    );
}
