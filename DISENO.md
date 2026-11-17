# Diseño visual de MiniTimer lite

Cuaderno es el tema claro y Panel nocturno es el tema oscuro. Ambos comparten la distribución, los controles y la jerarquía de la sesión.

## Temas

| Tema | Paleta |
| --- | --- |
| Cuaderno | Crema `#F8F3E8`, tinta `#252823`, verde pino `#285647`, beige `#EDE4D4` |
| Panel nocturno | Azul grafito `#121823`, panel `#202B3D`, blanco frío `#EDF3FF`, lima `#B6EF73` |

Instrument Serif identifica los títulos en ambos temas. Inter se usa para el texto, los controles y las cifras tabulares del tema claro. Panel nocturno usa IBM Plex Mono para las cifras.

La app inicia con el tema claro. El icono de luna cambia al tema oscuro y el icono de sol cambia al tema claro. La preferencia se guarda en el dispositivo. Cambiar de tema conserva la pantalla actual, los valores y la configuración de la sesión.

La maqueta HTML usa fuentes del sistema y funciona sin conexión. Las familias propuestas para la app se incluirían en el paquete de instalación.

## Controles y jerarquía

- Inicio muestra el nombre y la descripción opcional de cada sesión. El nombre es lo primero que se lee.
- La pantalla de sesión mantiene los números alineados y coloca los botones de cada contador en la misma posición. El objetivo es acceder a las tres filas sin desplazarse en un teléfono de tamaño habitual. Cuando no caben todos los contadores activos, la lista se desplaza. Se comprobará también con texto ampliado.
- Los botones tienen un área táctil mínima propuesta de 48 dp. Sumar tiene un relleno sólido y restar conserva un contorno visible.
- Un texto breve junto al primer contador identifica dónde se aplican los botones físicos de volumen. La indicación sigue al primer contador cuando cambia el orden.
- El total, si está visible, ocupa una banda propia y se distingue de los valores editables. La inclusión de cada contador puede configurarse durante su edición en la app. La maqueta expone ese ajuste en cada fila para probar el cálculo.
- Editar, restablecer y borrar se acceden desde acciones secundarias para dejar espacio a los controles de conteo.

## Referencias

- **Flighty** inspira la jerarquía de datos que deben leerse de un vistazo. [Detrás del diseño de Flighty](https://developer.apple.com/news/?id=970ncww4).
- **Tally** es una referencia funcional para agrupar contadores y ajustar sus valores. [Tally en App Store](https://apps.apple.com/us/app/tally-quick-counter/id957912407).
- **Counter de Roman Tsukanov** es una referencia funcional para el conteo táctil y con los botones de volumen en Android. [Counter en F-Droid](https://f-droid.org/packages/me.tsukanov.counter/).

**Editar sesión** abre una sola pantalla para cambiar el nombre de la sesión y configurar sus contadores. Allí se pueden agregar, renombrar, reordenar y elegir cuántos y cuáles quedan activos. Quitar un contador de los activos conserva su valor y lo quita del total. El campo calculado siempre se titula **Total**.

El lápiz junto al nombre abre la edición de la sesión. Si hay varios contadores activos, cada fila tiene un asa para reordenarla mediante una pulsación prolongada y un arrastre.

La pantalla de bienvenida usa el tema seleccionado. El logo y el nombre se sitúan en la parte superior y el botón **Comenzar** queda al pie. Cada contador tiene un lápiz junto a su nombre para editar el valor.

El botón **Exportar** queda fijo al pie de la sesión mientras se desplaza la lista de contadores.
