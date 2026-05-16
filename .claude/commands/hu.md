---
description: Implementa una HU (o dominio) de punta a punta — backend + frontend — y actualiza el estado
argument-hint: <HU-BE-014 | HU-FE-011 | caja | mora | ...>
---

Implementa la(s) Historia(s) de Usuario: **$ARGUMENTS**

Usa el agente `implementar-hu` para hacerlo. El agente debe:

1. Leer la(s) HU en `docs/hu/03_historias_usuario_backend.md` y `docs/hu/04_historias_usuario_frontend.md`. Si `$ARGUMENTS` es un nombre de dominio (ej. "caja"), agrupar las HU de backend y frontend de ese dominio.
2. Implementar **backend y frontend en la misma pasada**, siguiendo las convenciones del proyecto y el diseño de `.claude/agents/diseno-recaudo.md` (sin leer las imágenes PNG).
3. Compilar una sola vez cada lado al final y corregir errores.
4. Actualizar `docs/hu/ESTADO-HU.md` marcando lo completado.
5. Reportar qué quedó listo, cómo probarlo y los pendientes.

Si `$ARGUMENTS` viene vacío, muestra el contenido de `docs/hu/ESTADO-HU.md` para que el usuario elija qué HU implementar.
