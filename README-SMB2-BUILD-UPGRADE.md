# Actualización de build y SMB (SMB1 -> SMB2/3)

Este paquete moderniza **solo el sistema de compilación** y cambia jCIFS (SMB1) por **jcifs-ng (SMB2/SMB3)**.

## Cambios clave
- Gradle Wrapper: **5.6.4**
- Android Gradle Plugin: **3.5.4**
- Java 8 habilitado (compileOptions)
- Dependencia: `jcifs:jcifs:1.3.x` -> **`eu.agno3.jcifs:jcifs-ng:2.1.10`**
- `jcifs.properties` con `minVersion=SMB2` / `maxVersion=SMB311`
- `Application.onCreate()` fuerza SMB2/3 al inicio
- Workflow de GitHub Actions con **JDK 8** y auto-instalación de SDK `android-23` + build-tools `23.0.3`
- `versionName` = **1.1**, `versionCode` +1

## Cómo compilar en GitHub (gratis)
1. Sube esta carpeta a un nuevo repo (puede ser público o privado en GitHub Free).
2. Ve a **Actions → Build APK → Run workflow**.
3. Al terminar, descarga el artefacto **SmartLeopardHD-v1.1-debug**.

## Requisitos en el NAS (Synology)
- SMB mínimo = **SMB2**, máximo = **SMB3**
- **NTLMv1 desactivado**
- Usuario `huertas` de solo lectura sobre la carpeta de películas
