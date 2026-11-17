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

## Código y documentación

El modelo, la interfaz y los temas usan Kotlin y Compose Multiplatform en
`commonMain`. Android aporta el guardado atómico en el almacenamiento privado
y los eventos de volumen. iOS guarda el archivo dentro de su propio espacio
de aplicación y usa los controles de conteo en pantalla.

- [Especificación funcional](ESPECIFICACION.md)
- [Diseño visual](DISENO.md)
