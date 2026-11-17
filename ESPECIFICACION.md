# MiniTimer lite para Android y iOS

Especificación funcional v0.13.

## Objetivo

Registrar personas u otras cantidades en el momento y retomar el conteo después. Para la iglesia, cada servicio puede tener su propia sesión, con acceso simultáneo a los conteos de asistentes, equipo de servicio y visitantes nuevos.

## Sesiones

- Al abrir la app aparece una bienvenida con el botón **Comenzar**, que abre la lista de sesiones guardadas. Cada fila muestra el nombre de la sesión, su descripción si existe.
- Desde la lista puedes crear, abrir, editar y borrar sesiones. Cada sesión tiene un nombre y una descripción opcional. Una sesión nueva propone un nombre disponible, como **Sesión 1**, y trae **Contador 1** activo y en cero. Puedes guardar el formulario con esos valores. Cada sesión debe tener al menos un contador. Los contadores se crean activos. La opción **Activo** aparece al editar una sesión guardada.
- Dentro de cada sesión puedes crear, editar, borrar y reordenar sus contadores. Cada contador tiene un nombre y un valor entero.
- La app no impone un límite a la cantidad de sesiones ni de contadores guardados o activos por sesión. Si los activos no caben en pantalla, aparecen en una lista desplazable.
- La selección y el orden de los contadores activos se guardan por sesión. El primer contador activo es el que aparece arriba. Cuando hay más de un activo, mantén presionado el asa de un contador y arrástralo a su nueva posición en la pantalla de conteo.

- Dentro de una sesión, el botón de lápiz junto al nombre abre **Editar sesión**, una sola pantalla para editar sus datos, configurar cuántos contadores están activos y escoger cuáles se muestran. Desde esa pantalla puedes agregar, renombrar, reordenar y borrar contadores. Reducir el número de activos conserva los contadores que queden inactivos y sus valores.

## Conteo

- Cada contador nuevo empieza en cero. Los botones en pantalla suman o restan una unidad al contador correspondiente.
- En Android, con la pantalla de conteo visible y el dispositivo desbloqueado, subir volumen suma uno y bajar volumen resta uno al primer contador activo. En las demás pantallas y con el dispositivo bloqueado, los botones conservan su función normal.
- El lápiz junto al nombre de cada contador permite editar su valor. La app solo acepta números enteros.
- Cada sesión tiene un ajuste para permitir valores negativos. Está desactivado por defecto. En ese modo, restar en cero deja el valor en cero y la edición rechaza números negativos.
- Si quieres desactivar los negativos en una sesión que contiene algún valor negativo, primero debes corregir esos valores. La app indica el motivo y conserva los valores actuales.
- Restablecer devuelve el contador a cero después de confirmar la acción.
- Borrar un contador o una sesión requiere confirmación. Al borrar una sesión se borran también sus contadores.

## Guardado local

La app guarda automáticamente cada cambio en el dispositivo. Si cierras la app con un contador en 27, al volver a abrir su sesión ves 27. La creación, la edición, el conteo y el borrado funcionan sin conexión. El uso de la app no requiere una cuenta.

## Exportación

En la pantalla de conteo, **Exportar** permanece al pie y copia al portapapeles el nombre y valor de todos los contadores, en el orden de la sesión. La primera línea contiene el total calculado con el formato `total - valor`. Cada contador ocupa una línea con el formato `nombre - valor`. El texto incluye los contadores inactivos y los excluidos del total.

## Total de la sesión

La persona que usa la app define qué representa cada contador y decide cuáles contribuyen al total. Cada acción de conteo cambia únicamente el contador que se está usando.

- Cada sesión tiene la opción de mostrar un campo **Total**. Su valor es la suma de los contadores activos incluidos.
- Cada contador nuevo se incluye por defecto. Puedes desactivar **Incluir en el total** en cualquier contador y volver a activarlo cuando quieras. La exclusión se aplica al contador completo y conserva su valor.
- El campo tiene el título fijo **Total** y su valor es de solo lectura. Se recalcula al cambiar un valor, la selección de activos o la inclusión en el total. Si no hay contadores activos incluidos, muestra cero.
- La app guarda en el dispositivo la visibilidad del total y la selección de contadores incluidos.

## Apariencia

La app incluye dos temas con la misma distribución. Cuaderno es el tema claro y Panel nocturno es el oscuro. El tema inicial es claro. El botón de luna activa el tema oscuro y el botón de sol vuelve al tema claro. La preferencia se guarda en el dispositivo.

## Plataformas

El proyecto usa Kotlin Multiplatform y Compose Multiplatform. Android e iOS comparten las pantallas, ambos temas, el cálculo del total y el formato de exportación. Cada plataforma guarda los datos en su almacenamiento privado. El manejo de botones físicos está en la integración Android. El alcance inicial de iOS usa los botones en pantalla. Las interfaces públicas de Apple documentadas para pulsaciones físicas en este contexto están orientadas a la captura con cámara. [Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/supported-platforms.html) · [Controles de captura de Apple](https://developer.apple.com/videos/play/wwdc2025/253/)

## Criterios de aceptación

- En modo avión, puedes crear una sesión con descripción y agregar tres contadores con sus propios nombres y volver a encontrarlos al cerrar y abrir la app.
- En Android, con tres contadores activos, pulsar el botón en pantalla del segundo cambia solo ese contador. Tras moverlo al primer lugar, los botones de volumen actúan sobre él.
- En Android, los botones de volumen no alteran el conteo desde Inicio ni con la pantalla bloqueada.
- Puedes activar cuatro o más contadores en una sesión y acceder a ellos al desplazarte. Al reabrir la sesión se conserva la selección y el orden.
- En una sesión con negativos desactivados, restar en cero conserva cero. En una sesión con negativos activados, restar en cero da −1. La edición rechaza decimales en ambos casos.
- Si cambias un valor a 27, cierras la app y vuelves a abrir la sesión, el valor sigue en 27. Si confirmas el restablecimiento, vuelve a cero.
- Si confirmas el borrado de una sesión, desaparece de Inicio y sus contadores no vuelven a aparecer al reabrir la app.
- Si los contadores tienen valores de 80, 10 y 5, el total muestra 95. Si excluyes el contador de 10, el total muestra 85 y ese contador conserva su valor. Si después dejas activo solo el contador de 80, el total muestra 80.
- Con una sesión abierta, puedes alternar el tema claro y oscuro sin perder la pantalla actual, el conteo ni la selección del total. Al reabrir la app, se conserva la preferencia de tema.
- Si cambias el nombre de la sesión y de un contador, los nombres nuevos se conservan al reabrir la app y el título del campo sigue siendo **Total**.
- Si reduces de tres a dos los contadores activos desde **Editar sesión** y luego vuelves a activar el tercero, este conserva su valor y su nombre.

- Al pulsar **Comenzar**, crear una sesión y guardar sin editar el formulario, se abre la sesión con **Contador 1** activo en cero.
