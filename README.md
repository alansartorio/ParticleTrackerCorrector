
# Instalación

##### Descargar la release mas reciente de [acá](https://github.com/alansartorio/ParticleTrackerCorrector/releases).

## En Linux:
##### Arch:
1) Instalar Java: `# pacman -S jre-openjdk`
2) Instalar OpenCV: `# pacman -S opencv`

##### Otros:
1) Instalar Java.
2) Buildear OpenCV desde el codigo con la opcion `BUILD_JAVA` habilitada.

## En Windows
1) Instalar Java JRE o JDK ([OpenJDK](https://jdk.java.net/18/)). Extraer el *.zip* y agregar la carpeta `bin` al Path.
2) Instalar [OpenCV](https://opencv.org/releases/): Extraer el *.zip* y agregar la carpeta `opencv\build\java\x64` a la variable de entorno Path.


# Compilación
Agregar el jar de OpenCV al repositorio Maven local (solo la primera vez):

`mvn install:install-file -Dfile=/usr/share/java/opencv.jar -DgroupId=org.opencv -DartifactId=opencv -Dversion=4.5.5 -Dpackaging=jar -DgeneratePom=true`

Compilar a `.jar`:

`mvn package`

El ejecutable se encuentra en `./target/CorrectorParticulas-*.jar` y las librerias necesarias en `./target/lib/*`


# Instrucciones de uso

Abrir programa .jar (`java -jar CorrectorParticulas-*.jar`)
**Archivo -> Importar Video** y elegir video de fondo  
**Archivo -> Importar CSV** y elegir archivo .csv (puede ser generado automaticamente por programa externo o guardado por el mismo programa)  
**Archivo -> Exportar CSV** frecuentemente para no perder datos en caso de un crash del programa  

Cuando se importa un video de mas de 15 FPS, se activa automaticamente el modo de altos FPS, en el que las flechas avanzan de a 4 frames en vez de a uno. Tambien esta la opcion de avanzar y retroceder de a 1 frame (manteniendo presionado ctrl al apretar las flechas), pero no se podran crear ni modificar particulas en un frame no multiplo de 4 (es solo para visualizar y entender mejor el video).

# Controles  
### Teclado  
**flecha derecha:** avanzar frame  
**flecha izquierda:** retroceder frame  
**Crtl+Derecha:** avanzar frame (especial para modo de altos FPS)  
**Ctrl+Izquierda:** retroceder frame (especial para modo de altos FPS)  
**flecha abajo:** mantener para ocultar particulas (para mejorar visibilidad)  
**Ctrl+Z:** deshacer última operación  
**Ctrl+Y:** rehacer  

### Mouse  
**Mover rueda del mouse:** ajusta zoom en la imagen  
**Arrastrar con rueda del mouse presionada:** mueve la imagen  
**Arrastrar click izquierdo y soltar sobre otra particula:** swap de identidades (en el mismo frame y en los siguientes)  
**Arrastrar o hacer click izquierdo:** mover particula en el frame  
**Arrastrar o hacer click izquierdo desde particula del frame anterior:** crear particula en el frame actual con el mismo id  
**Arrastrar particula A del frame anterior y soltar sobre particula B del frame actual:** particulas con identidad de B pasan a tener identidad de A (afecta a frame actual y siguientes)  
**Click izquierdo:** crear particula en el frame actual  
**Click derecho:** borrar particula del frame actual  

# Elementos del Menu  
## Configuraciones  
**-> Tamaño de Fuente -> \#\#** para ajustar el tamaño de la fuente  
**-> Tamaño de las particulas -> \#\#** para ajustar el radio de los circulos  
**-> Siguiente frame automatico al traer particula** para agilizar el seguimiento de una particula  
**-> Union entre particula anterior y actual** para visualizar mejor la conexion entre las particulas del frame anterior y el actual  
**-> Marcar centro de particulas** para que el centro de cada particula se vea como un punto  


# Aclaraciones acerca del archivo de salida
El archivo de salida es del tipo ".csv", el cual contiene los datos de cada particula en cada frame.  
Cada linea tiene los siguientes datos separados por coma: [frame],[y],[x],[id]  
El X e Y estan en pixeles, por lo que si el video original se escalo, los valores se tendran que multiplicar por un valor.  
En caso de que el archivo se haya guardado basandose en un video de altos fps, el numero de frame no va a ser el real, si no que va a estar dividido por 4.
