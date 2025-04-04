# bombsmultiplayer

## Run (v1.0.0)

```bash
    javac --module-path "path\to\javafx-sdk-21.0.6\lib" --add-modules javafx.controls,javafx.graphics -d bin src/module-info.java src/com/bombsmultiplayer/*.java
```

```bash
    java --module-path "path\to\JavaFX\javafx-sdk-21.0.6\lib;bin" --add-modules javafx.controls,javafx.graphics -cp bin com.bombsmultiplayer.Main
```

## May be necessary create the following files

**.vscode/launch.json**
```json
    {
        "version": "0.2.0",
        "configurations": [
            {
                "type": "java",
                "name": "Launch JogoGUI",
                "request": "launch",
                "mainClass": "JogoGUI",
                "vmArgs": "--module-path \"path/to/javafx-sdk-XX/lib\" --add-modules javafx.controls,javafx.graphics"
            }
        ]
    }
```

**.vscode/settings.json**
```json
    {
        "java.project.referencedLibraries": [
            "path/to/javafx-sdk-XX/lib/*.jar"
        ]
    }
```