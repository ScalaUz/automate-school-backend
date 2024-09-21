{
  description = "A flake for getting started with Scala.";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = {
    self,
    nixpkgs,
    flake-utils,
  }: let
    supportedSystems = [
      "aarch64-darwin"
      "aarch64-linux"
      "x86_64-linux"
      "x86_64-darwin"
    ];
  in
    flake-utils.lib.eachSystem supportedSystems (
      system: let
        pkgs = import ./pkgs.nix nixpkgs system;

        makeShell = p:
          p.mkShell {
            buildInputs = with p; [
              ammonite
              coursier
              jdk
              mill
              sbt
              scala-cli
              scalafmt
            ];

            shellHook = ''
              # Print a message to indicate that the shellHook is being executed
              echo "Executing shellHook for ${system}"

              # Set environment variables by sourcing the .env file
              if [ -e .env ]; then
                set -a
                source .env
                set +a
              else
                echo ".env file not found. Make sure it exists in the expected location."
              fi
            '';
          };
      in {
        devShells = {
          default = makeShell pkgs.default;
          java17 = makeShell pkgs.pkgs17;
          java11 = makeShell pkgs.pkgs11;
          java8 = makeShell pkgs.pkgs8;
        };

        formatter = pkgs.default.alejandra;
      }
    );
}