# MiniTimer lite

App de conteo offline para Android 8.0 o posterior y iOS 17 o posterior.

## Android

Abre la carpeta `MiniTimer lite` en Android Studio y selecciona `composeApp`.
Usa JDK 21, Android SDK 36 y Android SDK Build-Tools 36.0.0. Desde la terminal:

```sh
./gradlew :composeApp:assembleDebug :composeApp:testDebugUnitTest
```

El APK de depuración se genera en
`composeApp/build/outputs/apk/debug/composeApp-debug.apk`.

## iOS

Abre `iosApp/MiniTimer.xcodeproj` en Xcode y ejecuta el esquema **MiniTimer**
en un simulador de Apple Silicon. Xcode llama a Gradle para compilar el
framework compartido. Configura JDK 21 y Android SDK 36 para Gradle. Si
inicias Xcode desde Finder, deja JDK 21 disponible para `/usr/libexec/java_home`
o inicia Xcode desde un entorno con `JAVA_HOME` configurado. Para instalar en
un iPhone, elige tu equipo de firma en el destino MiniTimer.

## Usar la app

Pulsa **Comenzar** y crea una sesión. El formulario propone un nombre y trae
**Contador 1** activo en cero. Puedes guardar directamente o agregar más
contadores. En **Editar sesión** puedes cambiar los nombres, el orden y la
participación en el total. Marca **Activo** en los contadores que quieras ver.

En Android, los botones físicos de volumen suman o restan una unidad al
primer contador activo. El lápiz de cada contador permite editar su valor.
El menú ofrece **Restablecer**. Los cambios se guardan en el dispositivo.
**Exportar** copia el total en la primera línea y agrega cada contador debajo.

## Cómo está implementada

### Código compartido

[composeApp/src/commonMain](composeApp/src/commonMain) contiene la interfaz de
Compose Multiplatform, los temas y las reglas de conteo. Android e iOS abren
la misma pantalla raíz, `ContadoresApp`. Cada plataforma proporciona el
guardado mediante `NotebookStore` y la función de portapapeles.

En [Notebook.kt](composeApp/src/commonMain/kotlin/app/contadores/Notebook.kt),
`Notebook` reúne las sesiones y el tema elegido. Cada `Session` mantiene sus
contadores en orden y calcula el total a partir de los activos incluidos.
`NotebookStore` guarda una copia de los datos antes de publicar el nuevo
estado a Compose. Si falla la escritura, la pantalla conserva el último
estado guardado y muestra un error.

**Exportar** prepara el texto en Kotlin compartido. La primera línea es
`total - valor` y las líneas siguientes contienen todos los contadores en el
orden de la sesión. Cada plataforma recibe ese mismo texto y lo copia con su
API nativa.

### Android

[MainActivity.kt](composeApp/src/androidMain/kotlin/app/contadores/MainActivity.kt)
monta la interfaz compartida con `setContent`. Su `AndroidViewModel` lee
`notebook.json` del almacenamiento privado de la app y guarda los cambios
escribiendo primero un archivo temporal y luego reemplazando el anterior
de forma atómica.

La actividad copia el texto con `ClipboardManager`. También recibe las
pulsaciones de volumen y las dirige al primer contador activo mientras la
pantalla de conteo está en primer plano y el dispositivo está desbloqueado.

### iOS

[AppDelegate.swift](iosApp/AppDelegate.swift) crea la ventana con UIKit.
El puente de Kotlin en
[MiniTimerHost.kt](composeApp/src/iosMain/kotlin/app/contadores/MiniTimerHost.kt)
monta la interfaz compartida con `ComposeUIViewController`. Swift lee y
guarda `notebook.json` en **Application Support** con escritura atómica.
También copia el texto con `UIPasteboard`. El conteo se hace con los botones
en pantalla.

El proyecto de Xcode ejecuta `:composeApp:embedAndSignAppleFrameworkForXcode`
en una fase de compilación. Gradle prepara `MiniTimerShared` para el iPhone o
el simulador ARM64 e incluye los recursos de Compose, como las fuentes.

## Documentación

- [Especificación funcional](ESPECIFICACION.md)
- [Diseño visual](DISENO.md)
