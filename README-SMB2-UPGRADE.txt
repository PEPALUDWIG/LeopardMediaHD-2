Leopard Media HD — Upgrade a SMB2/3 (elimina SMB1)

Qué he cambiado:
1) Sustituido jCIFS (SMB1) por jcifs-ng (SMB2/SMB3) como dependencia Maven:
   - app/build.gradle: añadido 'eu.agno3.jcifs:jcifs-ng:2.1.10'
   - app/build.gradle: eliminada referencia a libs/jcifs.jar (el fichero puede seguir en la carpeta, pero ya no se usa)

2) Forzado el dialecto SMB mínimo y máximo:
   - En la clase MizuuApplication.java se añaden:
       System.setProperty("jcifs.smb.client.minVersion","SMB2");
       System.setProperty("jcifs.smb.client.maxVersion","SMB311");
     y se sustituye el antiguo jcifs.Config.setProperty(...) por System.setProperty(...).

3) Añadido workflow de GitHub Actions para compilar APK de debug sin instalar nada en tu PC:
   - .github/workflows/build.yml

Cómo compilar (GitHub Actions):
1) Crea un repositorio nuevo en GitHub y sube todo este árbol.
2) Ve a la pestaña "Actions" → ejecuta el workflow "Build APK (legacy)".
3) Tras unos minutos, en la run verás un "Artifact": LeopardMediaHD-debug-apk → descárgalo (APK de debug).

Ajustes de tu NAS Synology:
- Panel de control → Archivo → SMB → Configuración avanzada: Mínimo = SMB2, Máximo = SMB3 (NTLMv1 desactivado).
- Usuario de solo lectura para la TV (por ejemplo "huertas").

Posibles errores y solución:
- Si la compilación falla con "Unsupported class file version 52.0" o similar, la causa es que el proyecto usa herramientas antiguas
  (AGP 1.3 + Build-Tools 23) que no aceptan librerías compiladas con Java 8. En ese caso hay dos soluciones:
  A) Subir el "Android Gradle Plugin" y el Gradle wrapper a versiones más recientes (p.ej. AGP 3.5.4 + Gradle 5.6.4) y volver a
     lanzar el workflow (recomendado). 
  B) Probar con una versión de jcifs-ng compilada para Java 7 (si disponible).
  Si quieres que te deje hecho el plan A, dímelo y preparo otro paquete con los archivos de build modernizados.

Notas:
- El código fuente (Java) permanece igual; solo se ha cambiado la librería y la configuración de dialectos SMB.
- jcifs-ng mantiene el paquete 'jcifs.smb.*', por lo que las clases como SmbFile siguen funcionando pero ahora negocian SMB2/3.
