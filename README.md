# Instrucciones de uso

Abrir programa .jar  
"Archivo -> Importar Video" y elegir video de fondo  
"Archivo -> Abrir" y elegir archivo .csv (puede ser generado automaticamente por programa externo o guardado por el mismo programa)  
"Archivo -> Guardar" frecuentemente para no perder datos en caso de un crash del programa  

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
**Arrastrar click izquierdo y soltar sobre otra particula:** swap de identidades (en el mismo frame y en los siguientes)  
**Arrastrar o hacer click izquierdo:** mover particula en el frame  
**Arrastrar o hacer click izquierdo desde particula del frame anterior:** crear particula en el frame actual con el mismo id  
**Arrastrar particula A del frame anterior y soltar sobre particula B del frame actual:** particulas con identidad de B pasan a tener identidad de A (afecta a frame actual y siguientes)  
**Click izquierdo:** crear particula en el frame actual  
**Click derecho:** borrar particula del frame actual  

# Elementos del Menu  
## Configuraciones  
**-> Escalas -> \#\#\#%** para que se vea mejor  
**-> Tamaño de Fuente -> \#\#** para ajustar el tamaño de la fuente  
**-> Tamaño de las particulas -> \#\#** para ajustar el radio de los circulos  
**-> Siguiente frame automatico al traer particula** para agilizar el seguimiento de una particula  
**-> Union entre particula anterior y actual** para visualizar mejor la conexion entre las particulas del frame anterior y el actual  
**-> Marcar centro de particulas** para que el centro de cada particula se vea como un punto  
