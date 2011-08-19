Martus(tm) Software Versión 3.4 README_es.txt
---------------------------------------------

Contenido:
A. Cambios de la versión 3.3.2 a la versión 3.4   (03-2010)
B. Cambios de la versión 3.3 a la versión 3.3.2   (08-2009)
C. Cambios de la versión 3.2 a la versión 3.3     (08-2008)
D. Cambios de la versión 3.1 a la versión 3.2     (09-2007)
E. Cambios de la versión 3.0 a la versión 3.1     (04-2007)
F. Cambios de la versión 2.9 a la versión 3.0     (09-2006)
G. Cambios de la versión 2.8.1 a la versión 2.9   (03-2006)
H. Cambios de la versión 2.8 a la versión 2.8.1   (11-2005)
I. Cambios de la versión 2.7.2 a la versión 2.8   (09-2005)
J. Cambios de la versión 2.7 a la versión 2.7.2   (08-2005)
K. Cambios de la versión 2.6 a la versión 2.7     (04-2005)
L. Cambios de la versión 2.5 a la versión 2.6     (02-2005)
M. Cambios de la versión 2.0.1 a la versión 2.5   (11-2004)
N. Cambios de la versión 2.0 a la versión 2.0.1   (08-2004)
O. Cambios de la versión 1.5 a la versión 2.0     (07-2004)
P. Cambios de la versión 1.2.1 a la versión 1.5   (02-2004)
Q. Cambios de la versión 1.2 a la versión 1.2.1   (12-2003)
R. Cambios de la versión 1.0.3 a la versión 1.2   (09-2003)
S. Cambios de la versión 1.0.2 a la versión 1.0.3 (05-2003)
T. Cambios de la versión 1.0.1 a la versión 1.0.2 (02-2003)
U. Cambios de la versión 1.0 a la versión 1.0.1   (01-2003)
V. Instrucciones especiales para actualizar Winsock, si
   tiene problemas al ejecutar Martus en Windows 95:

Visite el sitio web http://www.martus.org para mayor
información sobre Martus.

Consulte la Guía del Usuario para obtener instrucciones acerca
de cómo instalar Martus.

Para obtener la documentación actual en todos los idiomas
disponibles, así como las versiones anteriores, visite el
sitio web http://www.martus.org/downloads/.


A. Cambios de la versión 3.3.2 a la versión 3.4

Esta versión está disponible en inglés y en otros
idiomas indicados en la página http://www.martus.org de
descargas del programa. En el futuro habrá versiones en otros
idiomas, como paquetes de idioma en la página de documentación 
(http://www.martus.org/downloads)

- Se agregó la capacidad de requerir que ciertas columnas 
  de tabla sean ingresados antes que un boletín pueda ser 
  guardado. 
- A la funcionalidad de hacer búsquedas se agregó la capacidad 
  de satisfacer los parámetros de búsqueda que son columnas 
  de tabla en una sola fila de datos en una tabla de un 
  boletín. Si quiere satisfacer los parámetros de búsqueda 
  que son columnas de tabla en una sola fila de datos en una 
  tabla de un boletín, haga clic en la casilla de "Satisfacer 
  los parámetros de búsqueda que son columnas de tabla..." 
  Por ejemplo, si quiere buscar un nombre de víctima específico 
  en una fila sola en sus boletines creado después de una fecha 
  específica, seleccione la casilla y ingresa los siguientes 
  campos y parámetros en la ventana de Búsqueda: "Información 
  de la víctima: Nombre" = A y "Información de la víctima: 
  Apellido" = B y "Fecha de creación" >= AAAA-Mes-DD. Si no 
  selecciona el campo de "Satisfacer los parámetros de 
  búsqueda que son columnas de tabla...", Martus encontrará 
  boletines creado después de la fecha especificada, en que 
  cualquier fila tiene el nombre especificado y cualquier 
  otra fila tiene el apellido especificado, pero no 
  necesariamente en la misma fila del boletín. Por ejemplo, 
  puede tener una fila con "Nombre" = A y "Apellido" = C, 
  una otra fila con "Nombre" = D y "Apellido" = B, y Martus 
  encontrará este boletín porque considerará que satisface 
  los parámetros de la búsqueda porque no especificó que los 
  parámetros deben ser satisfecho en una fila sola.
- Se agregó la capacidad de requerir que Martus valide todas 
  las fechas usando rangos de fechas específicado por usuario. 
  (Por ejemplo, fechas de evento no pueden ser antes o después 
  de una fecha específica. Incluye la capacidad de especificar 
  que fechas no pueden ser después de "hoy" - los eventos no 
  pueden ser en el futuro). Nota por favor que puede requerir 
  la validación de fechas en los campos predeterminados y los 
  campos personalizados. En campos de fecha, las opciones en 
  la lista desplegable reflejarán los rangos específicado por 
  usuario en la personalización de la plantilla. 
- Se agregó la capacidad de capturar 'Historial extendido del 
  boletín' a todos los usuarios que lo editan. Eso puede ver 
  con el botón de 'Detalles de boletín'. Una cuenta central 
  que crea una versión nueva del boletín puede ver el código 
  público y identificación del boletín de las versiones del 
  autor previo. La información del autor previo y de 
  la identificación del boletín es agregado al XML de la 
  exportación de cualquier boletines creado con historial 
  extendido.
- Se corregió la funcionalidad de Modificar un boletín 
  existente para que Martus reconozca si la parte siempre 
  Privada ha cambiado, y pregunte al usuario si quiere usar 
  los campos viejos o los campos nuevos. 
- Se agregó un mensaje de advertencia que Martus muestra si 
  el usuario trata de importar un archivo de XML que es 
  incompatible porque fue creado por una versión diferente 
  de Martus. 
- Se corrigió errores pequeños, aclaraciones y correcciones 
  a la pantalla del usuario.
- Se actualizó la versión del Manual Usuario en inglés. 


B. Cambios de la versión 3.3 a la versión 3.3.2

- Se solucionó varios temas relacionados a la ampliación 
  de tablas conteniendo listas desplegables con 
  procesamiento de "datos-derivados", incluyendo comportamiento 
  lentoso o colgarse de la interfaz gráfica después de usar 
  la tecla 'Tab' para seguir de un campo de una tabla ampliada 
  que es la fuente para una lista desplegable con procesamiento 
  de "datos-derivados" (y el campo de la lista desplegable 
  con "datos-derivados" también está en una tabla ampliada); 
  minimizando una tabla que contiene una lista desplegable 
  con "datos-derivados", o guardando un boletín con una tabla 
  ampliada que contiene una lista desplegable de "datos-derivados."
- Se corregió el problema en que campos personalizados de 
  múltiples líneas no fueron mostrados durante la creación 
  de reportes de página. Nota por favor que reportes de página 
  creados con versiones anteriores de Martus todavía van a tener 
  este problema - los usuarios deben crear reportes de página 
  de nuevo para ver la corrección.
 

C. Cambios de la versión 3.2 a la versión 3.3

- Se agregó la capacidad de sellar muchos borradores al mismo 
  tiempo en una "hornada." Para hacer este, vaya a Editar > 
  Sellar boletín(es).
- Nueva funcionalidad a agregar una cuenta central a un grupo 
  de boletines o a una carpeta entera de boletines, al mismo 
  tiempo. Para hacer este, vaya a Editar > Actualizar acceso 
  de oficina central. Esta opción será coloreada el color gris 
  (no disponible) a menos que al menos un boletín sea 
  seleccionado y al menos un HQ es configurado. Martus mostrará 
  una barra de progreso durante la actualización y permitirá 
  que anule si desea. Los borradores serán actualizados para 
  reflejar la nueva información de HQ. Para boletines sellados, 
  Martus generará automáticamente una nueva versión sellada de 
  cada boletín.
- Se agregó la capacidad de ver muchos tipos de adjuntos de 
  imagenes dentro de Martus mientras viendo, creando, y 
  modificando boletines, y también en la vista anticipada de 
  Detalles del boletín.
- Se corrigió errores pequeños, aclaraciones y correcciones 
  a la pantalla del usuario.


D.  Cambios de la versión 3.1 a la versión 3.2

- Nueva funcionalidad que le permite mostrar el(los) 
  boletin(es) de las siguientes maneras: 1) Ocultar/Mostrar 
  campos largos que ocupan bastante espacio, 2) Crear secciones 
  en sus boletines que también pueden ocultar/mostrar, 3) Mostrar 
  la tabla con una vista ampliada (incluyendo la capacidad de 
  agregar filas desde esa vista), 4) Colocar campos al costado 
  de cada uno en una fila, 5) A principios de la sesión, Martus 
  automáticamente reduce el tamaño de las tablas para ahorrar 
  espacio.
- Se agregó la capacidad de requerir que ciertos campos sean 
  ingresados antes que un boletín pueda ser grabado.
- Se agregó la capacidad de crear la lista desplegable con 
  procesamiento de "datos-derivados", donde los valores en un 
  campo de la lista desplegable se basan en los datos ingresados 
  en otro campo de una tabla en otra parte del boletín. Por favor 
  note que la fuente de datos y la lista desplegable que resulta 
  no pueden estar en el mismo campo de una tabla. 
- Se agregó un mensaje de diálogo del estado del progreso de 
  la búsqueda, y la capacidad de cancelar las búsquedas.
- Se mejoró el funcionamiento y se aumentó la velocidad de las 
  búsquedas y otras operaciones del boletín, especialmente para 
  los usuarios con una gran cantidad de boletines.
- Se solucionó varios temas, incluyendo: 1) los ajustes de 
  configuración (ejem. Oficina(s) Central(es)) que no podían ser 
  guardados en aquellos usuarios con una gran cantidad de 
  personalizaciones a su boletín, 2) los errores al arrastrar los 
  boletines mientras se enviaban a/descargaban del servidor.
- Se mejoró mensajes dirigidos a los usuarios (ejem. errores 
  de personalizar campos.)
- Se corrigió errores pequeños, aclaraciones y correcciones 
  a la pantalla del usuario.


E. Cambios de la versión 3.0 a la versión 3.1    
 
- Se agregó una nueva funcionalidad que notifica a las 
  Oficinas Centrales si existen boletines de Oficina Regional 
  a bajar. Para habilitarla en su cuenta Martus, ir a Opciones > 
  Preferencias y seleccionar "Comprobar automáticamente si 
  existen nuevos boletines de Oficina Regional". Aproximadamente 
  cada hora, aparecerá un mensaje en la barra de estado (en la 
  esquina inferior izquierda de la pantalla) indicando que 
  Martus está comprobando la existencia de nuevos boletines de 
  Oficina Regional. De haber boletines de Oficina Regional a 
  bajar, la barra de estado mostrará otro mensaje. En este punto, 
  puede ir al  menú Servidor para cargar la pantalla Recuperar. 
- Se agregó la capacidad de ordenar los boletines en la 
  pantalla Recuperar, haciendo click en el título de la columna. 
  La funcionalidad de ordenar es sólo ascendente.
- Se mejoró el funcionamiento y los posibles problemas de 
  memoria al ingresar y salir de Martus para usuarios con 
  grandes cantidades de boletines.
- Se agregó un mecanismo para acelerar la carga de Martus 
  y la navegación cuando una cuenta tiene un gran número de 
  boletines. Puede agregar " --folders-unsorted" al final de 
  la orden en su atajo de Martus en su Desktop. Este hará que 
  carpetas en Martus no sean clasificadas ("sorted") cuando al 
  principio los carga (porque la clasificación puede tomar 
  tiempo cuando tiene muchos boletines.) Siempre puede hacer 
  clic en un encabezado de columna en la lista de vista 
  anticipada de boletines para clasificar la carpeta si desea, 
  pero incluyendo esta opción ahorrará el tiempo en la carga 
  y en la entrada en nuevas carpetas en Martus.
- Se corrigió los siguientes errores ingresados en la versión 
  3.0, incluyendo:  
  1) No se muestra los campos por rango de fecha en página de 
     reporte.
  2) La personalización de la sección privada se pierde al 
     salir de Martus.
  3) Problemas al seleccionar archivos desde una MAC (Por 
     ejemplo, al configurar las Oficinas Centrales, adjuntando 
     archivos a los boletines, recuperando su clave e importando 
     las plantillas personalizadas), 4) Inconsistencia al 
     mostrar las fechas arábicas en los reportes entre el 
     detalle del boletín y el conteo-resumen.
- Numerosas pequeñas correcciones al programa, aclaraciones 
  y limpieza de las pantallas del programa.


F. Cambios de la versión 2.9 a la versión 3.0

- Se agregó la funcionalidad de reporte. Los reportes muestran 
  los resultados de los boletines que encajan con un cierto 
  criterio de búsqueda, pueden ser impresos o grabados en un 
  archivo. Los informes pueden contener un subconjunto de 
  campos en los boletines, pueden ser formateados como una 
  tabla con una fila por cada boletín. Los reportes pueden ser
  agrupados y clasificados por diferentes campos, 
  con un conteo resumen de los boletines en cada agrupación.
- Se agregó la capacidad de personalizar los formatos de la 
  parte inferior/sección privada de los boletines. 
- La nueva funcionalidad Importar permite a los usuarios importar
  datos electrónicos en el formato de boletines de Martus, 
  incluyendo tanto texto como archivos adjuntos. También se 
  actualizó la funcionalidad Exportar para que encaje con la 
  estructura de importación, permitiendo ahora la exportación 
  de archivos adjuntos.
- Se mejoró la funcionalidad de búsqueda incluyendo la capacidad 
  de buscar dentro de un mapa personalizado, fusionando campos 
  similares en la opción campo-selección-lista y se clarificó 
  cuando múltiples campos tienen los mismos títulos.
- Se cambió el año por defecto en las fechas de los boletines 
  a "Desconocido", en vez del año en curso.
- Significativas mejoras en el desempeño de las cuentas con un gran
  número de boletines, específicamente al cargar la pantalla Recuperar. 
  Se agregó mensajes adicionales para el usuario sobre el estado de
  las operaciones que podrán ser prolongadas.
- Se mejoró la personalización, incluyendo mensajes adicionales 
  a los usuarios y exhibir los títulos largos de campos 
  personalizados en líneas múltiples.


G. Cambios de la versión 2.8.1 a la versión 2.9

- Con el lanzamiento de la versión 2.6 del servidor (Marzo 2006),
  se aceleraron varias operaciones importantes del servidor /
  cliente.  Específicamente, ahora son más rápidas las siguientes
  operaciones: subiendo/enviando boletines, bajando tus propios
  boletines o boletines de las oficinas regionales, iniciando al
  conectarse el servidor con la cuenta del usuario.
- Mejoras en el funcionamiento de las cuentas con una gran
  cantidad de boletines. Optimiza la velocidad de las siguientes
  acciones: muestra de carpetas/ordenando/moviendo boletines, etc
- Se cambió las operaciones de bajada de boletines del servidor,
  para que ahora sucedan en un segundo plano (similar al envío de
  boletines a un servidor), es decir que Ud. puede seguir trabajando
  con Martus mientras esta operación se está ejecutando sin tener
  que esperar que la misma finalice. Cuando la operación termine,
  los boletines se presentarán en carpetas "Bajadas". Para cancelar
  la descarga, regrese al dialogo para "Bajar" boletines.
- Mensajes mejorados para el usuario acerca del estado del
  servidor.
- Se agregó la capacidad de buscar en columnas dentro de un
  mapa (GRID) (en vez del texto entero del mapa) al especificar
  el campo en una búsqueda avanzada y se agregó la opción de buscar
  solamente en las versiones más recientes de los boletines.
- Se corrigió los errores de búsqueda de las versiones 2.8 y 2.8.1.
  Específicamente se abordó el problema del resultado incorrecto
  de las búsquedas en los campos de la lista desplegada (DROPDOWN)
  con espacios en los valores escogidos y se incorporó la
  personalización del nombre de identificación (tags) en la lista
  de búsqueda de campos donde los títulos (labels) fueron dejados
  en blanco (ejem. para encabezados de secciones), de modo que no
  exista ningún valor en blanco en la lista de campo.
- Se agregó la capacidad de insertar y borrar filas en los mapas
  personalizados (GRID) y pantallas de búsquedas.
- Se usó todo el espacio de pantalla disponible al presentar los
  datos del boletín y el diálogo de la información de contacto.
- Se movió la opción "Reenviar boletines" para que esté bajo el
  menú del servidor (para oficinas centrales que realizan las
  copias de seguridad a los servidores de sus oficinas regionales
  que no tienen acceso a Internet)
- Se removió todos los mensajes desorientadores "no todos los
  boletines fueron bajados" que aparecían cuando una cuenta de
  oficina central no tenía permiso para ver las versiones antiguas
  de ciertos boletines.
- Se introdujo varias actualizaciones a las preferencias de fechas:
  localización de formatos de fecha, opciones adicionales de
  formato, cambios para presentar correctamente las fechas
  tailandesas y persas (y convertir las previamente ingresadas).
  Las fechas persas utilizan un algoritmo aritmético bien conocido
  para calcular los años bisiestos. También se creó una herramienta
  para ayudar a diagnosticar los ajustes de la fecha.
- Se hizo cambios para ayudar a los programas procesadores de texto
  a presentar correctamente los acentos contenidos en los reportes
  de archivos en html.
- Se agregó el kurdo a la lista desplegable de idiomas. Si necesita
  ayuda con la presentación de las fuentes kurdas en Martus, por
  favor contacte a help@martus.org
- Implementación inicial de una herramienta de importación de datos
  que permite la conversión de archivos de texto electrónicos (en
  formato .dsv o .xml) al formato de Martus. Esta versión inicial no
  maneja la importación de mapas (GRIDS) personalizados ni adjuntos,
  sin embargo, administra otro tipo de campos. Para instrucciones /
  ayuda sobre cómo ejecutar esta funcionalidad, por favor contacte
  a help@martus.org
- Se corrigió numerosos errores pequeños, aclaraciones y correcciones
  a la pantalla del usuario.


H. Cambios de la versión 2.8 a la versión 2.8.1

Hay un problema en la versión 2.8.1 con las búsquedas en
campos personalizados al utilizar la opción listas
desplegadas, cuando las opciones de la lista desplegada
contienen espacios entre palabras. Martus encontrará
correctamente los boletines si usted busca en "Todos
los campos" "contenido" los valores que aparecen en la
lista desplegada.  Sin embargo, el problema ocurre si
usted realiza una búsqueda en un campo específico de la
lista desplegada si ésta cuenta con espacios entre
palabras. Ello se arreglará en la próxima versión,
pero hasta entonces sugerimos que si usted personaliza
los boletines, cree opciones en la lista desplegada que
no tengan espacios entre palabras (si desea puede usar
guiones para enlazarlas).

- Reparación del problema en la versión 2.8, en el que se
  mostraban y archivaban las fechas y los rangos de fechas
  anteriores al 1 de enero de 1970, de manera incorrecta.
- Incorporación de las traducciones al tailandés y ruso de
  la versión 2.8.


I. Cambios de la versión 2.7.2 a la versión 2.8

Esta versión está disponible en inglés y en otros idiomas
indicados en la página http://www.martus.org de descargas
del programa. Otros idiomas estarán disponibles en el futuro,
como paquetes por idioma en la página de documentación (http://www.martus.org/downloads)

- Se agregó la posibilidad de crear columnas de diferentes
  tipos en los mapas (listas desplegadas, casillas de
  verificación, fechas y rangos de fechas)
- La función de búsqueda avanzada le permite ahora a los
  usuarios especificar los campos que desea incluir en su
  búsqueda (incluyendo campos personalizados), además de buscar
  en el texto de los boletines. Las búsquedas pueden combinar
  búsquedas en diferentes campos usando las opciones y/o.
- La mejorada funcionalidad de impresión permite imprimir
  múltiples boletines a la vez.
- Se ha añadido la posibilidad de guardar boletines
  seleccionados en un archivo html, con la opción de incluir
  o excluir la información privada.
- Se creó una nueva opción de menú "Organizar carpetas", que
  permite a los usuarios ordenar sus carpetas de la manera
  más conveniente.
- Se agregó advertencias para el usuario si es que la
  traducción del programa que está usando no es de la misma
  que la versión del programa, y se muestra la fecha de la
  traducción de los paquetes de idiomas en la ventana acerca
  del programa.
- Se muestra la imagen de Martus apenas se inicia el programa
  para que los usuarios sepan que el programa está cargando.
- Se actualizó los módulos de cifrado para que usen
  Bouncy Castle Java Cryptography Extensión.
- Se mejoró la manera en que se muestra e imprime los idiomas
  escritos de derecha a izquierda.
- Se incorporó fuentes del sistema para que se muestren ciertos
  idiomas en los menús (por ej. nepalí).
- Se cambió el comportamiento de los borradores para que
  reconozcan los cambios de configuraciones de oficinas
  centrales y campos personalizados que se hayan agregado.
- El Martus Server 2.4 y versiones posteriores pueden enviar
  noticias y mensajes a los usuarios de Martus Client cuando
  éstos se conecten (por ej. mensajes sobre nuevas versiones
  disponibles o advertencias de que el servidor no va a estar
  en línea por mantenimiento).
- Se modificó el método de verificación de archivos bajados
  del sitio web de MD5 a SHA1.
- Se actualizó la documentación en inglés (Tarjeta de
  referencia y Guía del usuario).
- Numerosos arreglos pequeños, aclaraciones y limpiezas de las
  pantallas de los usuarios.


J. Cambios de la versión 2.7 a la versión 2.7.2

- Se removió la traducción incompleta e inexacta del idioma
  Nepalí 2.0.1 del programa y se colocó un paquete de idioma
  Nepalí en el sitio web http://www.martus.org/downloads.  Este
  paquete de idioma contiene traducción al Nepalí de las pantallas
  del programa (que puede usar en las versiones 2.5 o mayor y que
  tiene el 90% de las palabras del programa traducidas al Nepalí),
  archivo de ayuda en linea (versión 2.0.1), Tarjeta de referencia
  (versión 2.0.1), Guía del usuario (versión 2.0.1), y archivo
  README (traducido parcialmente hasta la versión 2.6).

En Windows, para ejecutar el programa Martus en Nepalí en la
versión 2.7.2 y anterior tiene que modificar la línea de comando
para que los menues se muestren correctamente (en la línea de
comando, y en los enlaces de acceso directo o aliases que fueron
instalados en su ordenador).

Para iniciar el programa desde la línea de comando, vaya a la carpeta de
Martus y escriba:
C:\Martus\bin\javaw.exe -Dswing.useSystemFontSettings=false -jar C:\Martus\Martus.jar

Para cambiar los enlaces de acceso directo haga clic con el
botón derecho, elija Propiedades, y cambie el campo Commando a :
C:\Martus\bin\javaw.exe -Dswing.useSystemFontSettings=false -jar C:\Martus\Martus.jar


K. Cambios de la versión 2.6 a la versión 2.7

Esta versión sólo está disponible en inglés y en persa.
Otros idiomas estarán disponibles en el futuro, como un
"Paquete de idioma" en la página de documentación (http://www.martus.org/downloads)

- Capacidad adicional para crear un campo personalizado
  "lista desplegable" (no dentro de una mapa/tabla)
- Capacidad adicional para crear un campo personalizado de
  "mensajes" para aconsejar al usuario sobre cómo digitar datos,
  y para crear comentarios/notas que serán exhibidas en cada
  boletín (Ej. ayuda en-la-pantalla)
- Capacidad adicional para que una cuenta de Oficina Central
  exporte plantillas personalizadas para darle a usuarios
  regionales, o a usuarios para exportar sus propias plantillas.
  Luego, usuarios pueden importar configuraciones personalizadas
  desde opciones de plantillas.
- Cada cuenta de Oficinal Central configurada ahora puede ser
  habilitada o deshabilitada para cada boletín creado o modificado.
  Usuarios además pueden elegir que ciertas cuentas de Oficina Central
  se asignen a todos los boletines nuevos creados por defecto.
- Búsquedas ahora revisan versiones anteriores de cada boletín además
  de revisar la última versión
- Mejoras adicionales para visualizar idiomas escritos de
  derecha-a-izquierda
- Traducción al persa de la interfaz gráfica incluida
- El exportar a XML ahora incluye tipo de campo personalizado.


L. Cambios de la versión 2.5 a la versión 2.6

- Usuarios pueden buscar y ver todo el contenido de todas las
  revisiones de boletines sellados almacenados en su ordenador
  con oprimir el botón 'Detalles del boletín'
- Tiene la opción de bajar todas las revisiones de un boletín
  sellado de un servidor o solo la revisión mas reciente. Usuarios
  con poco espacio para almacenar o conexiones al Internet lentas
  pueden elegir solo bajar las revisiones mas recientes de un
  boletín de gran tamaño.
- Las búsquedas incluyen los nombres de archivos adjuntos.
- Exportar a XML incluye mayor soporte de campos personalizados y
  revisiones de boletines sellados.
- Se mejoró como se muestran idiomas escritos de derecha a
  izquierda (Ej. Árabe)
- El Manual de usuario y Guía rápida en Árabe ha sido incluido.
- Numerosas correcciones al programa, clarificaciones y
  correcciones a las pantallas del programa.
- Puede que algunas ventanas de Martus 2.6 vistas en Mac OS
  tengan problemas al mostrar textos Árabes.


M. Cambios de la versión 2.0.1 a la versión 2.5

- Se agregó la posibilidad de crear nuevas revisiones de boletines
  sellados y así poder hacer cambios o adiciones al boletín
  previamente sellado. Con esta versión del programa Martus solo
  podrá buscar y ver el contenido de la revisión más reciente de
  cada boletín. Con presionar el botón Detalles de boletín podrá
  ver el título, identificación de boletín y fecha guardada de
  todas las revisiones anteriores de un boletín que tenga guardado
  en su computador.
- Se agregó la habilidad de instalar y actualizar las traducciones
  del programa en cualquier momento después de que el programa se
  haya completado. Un "Paquete de idioma" para cada idioma
  (incluyendo inglés) puede contener la traducción del programa
  de Martus, la guía de usuario, tarjeta de referencia, archivo
  Readme y ayuda en línea. Los paquetes de idiomas serán
  distribuidos a través del sitio web de Martus.
- Cambios para acelerar el manejo de los boletines y carpetas
  (ej mover, ordenar boletines) fueron introducidos
- Mejoras a la característica de campos personalizados (ej.
  tamaño columnas de mapas)
- Se incluye la traducción al tailandés
- Se incluye la traducción al árabe
- Cambios para mostrar apropiadamente los idiomas escritos de
  derecha a izquierda como el árabe
- Varias mejorías al aspecto del programa Martus en Linux.
- Se arregló un problema en donde bajando o importando boletines con
  archivos adjuntos de gran tamaño causaba que Martus fallara
  con un error "out of memory" (falta memoria). Archivos adjuntos a
  un boletín que tengan menos de 20MB no sufren de este problema.
- Numerosas correcciones al programa, clarificaciones y
  correcciones a las pantallas del programa.
- Puede que haya problemas en las letras en varias ventanas de diálogo
  en el programa de instalación de Martus 2.5 si está usando Nepalí o
  Tailandés. Como es difícil probar en todas estas versión de Windows,
  por favor notifíquenos si es que nota problemas en la instalación.


N. Cambios de la versión 2.0 a la versión 2.0.1

- Se agrego una barra de desplazamiento horizontal para los
  mapas en los campos personalizados que sean más anchos que
  la pantalla.
- Martus está disponible en Francés
- La documentación en ruso y español han sido actualizadas
  con las características de la versión 2.0 del programa
- Clarificaciones menores y limpieza de la documentación en
  inglés
- Cambios al programa de instalación para solucionar
  actualizaciones en Windows 98 y Windows ME
- El sitio web de Martus ahora tiene la opción de bajar el
  programa en múltiples partes


O. Cambios de la versión 1.5 a la versión 2.0

- Ahora puede configurar múltiples cuentas de Oficinas centrales
  usando un interfaz diferente. Esta característica es útil si tiene
  a varias personas en su organización que van a evaluar los
  boletines que ingrese.
- Oficinas centrales pueden enviar boletines a un servidor a nombre
  de sus oficinas regionales que no tengan acceso a internet.
- Los campos personalizados han sido extendidos para que pueda crear
  campos personalizados de varios tipos  (ej. fecha, mapa, Si/No).
- El programa de instalación de Windows es NSIS de código abierto
  que puede usar alfabetos que no sean latinos.
- La carpeta "Boletines guardados" reemplaza a la "Bandeja de
  salida", "Boletines enviados" y "Borradores".
- Cada boletín tiene ahora una columna que indica si ha sido enviado
  a un servidor o no.
- La fecha en que un boletín fue guardado por última vez es mostrada
  en la vista previa de un boletín y en la información de los boletines.
- Al crear o modificar un boletín, el botón Enviar a sido cambiado a
  'Guardar sellado'
- La característica Borra-Todo de Martus 1.5 ha sido reemplazada
  por dos acciones: "Eliminar mis datos" que elimina los boletines y
  la clave de su cuenta; y "Eliminar todos los datos y remover Martus"
  que desinstala Martus y elimina la carpeta de Martus, junto con
  todas las cuentas que tenga. Esta característica solo es para usar
  en caso de emergencias.
- La característica para hacer copias de seguridad ha sido mejorada -
  ya no tiene que hacer una copia de seguridad antes de haber creado
  un boletín, pero se le recordará que necesita hacerla si es que no
  lo hace en un periodo de tiempo.
- Al buscar boletines tiene la opción de usar los términos 'y' y 'o',
  o sus equivalentes en los idiomas del programa y siempre tiene
  la opción de usar los términos en Inglés, 'and' y 'or'. Esto le
  permite ejecutar búsquedas a usuarios que no tengan teclados en sus
  idiomas nativos.
- Un nuevo botón "Detalles de boletín" le muestra el número de
  identificación único y las oficinas centrales que pueden ver los
  datos privados del boletín.
- Al imprimir ahora tiene la opción de incluir/excluir los datos
  privados.
- Puede ejecutar búsquedas en la ayuda en línea.
- Puede indicar si por motivos de seguridad no permite boletines
  públicos
- La característica para renombrar carpetas ha sido mejorada.
- Numerosos defectos han sido corregidos, clarificaciones y
  reconfiguración del interfaz del programa.


P. Cambios de la versión 1.2.1 a la versión 1.5

Le recomendamos que use el software Martus Client versión 1.5
con el servidor Martus versión 2.0 o más. Todos los servidores de
Martus han sido actualizados a la versión 2.0 al momento que el
software Martus Client versión 1.5 fue completado. Si usted esta
operado un servidor Martus es necesario que actualice el software
del servidor Martus a la versión 2.0. Este lo puede encontrar en
el sitio web www.martus.org.

- Una instalación de Martus puede tener múltiples cuentas de
  usuarios.  Esto introduce un cambio a la ventana de inicio de
  Martus. Ahora tiene la opción de iniciar su sesión bajo una
  cuenta existente de Martus, puede crear una nueva cuenta de
  Martus o puede recuperar una cuenta de una copia de reserva.
  Cada cuenta en una instalación de Martus tendrá su propia carpeta
  dentro de la carpeta de instalación.
- Al iniciar Martus puede elegir el idioma en que desea usar
  el programa.
- Se mejoró la capabilidad para hacer copias de reserva de sus
  claves. Ahora se pueden hacer "copias de seguridad múltiples"
  que puede distribuir entre personas de confianza.
- El comando Borra-Todo ahora lo puede usar para eliminar sus claves,
  boletines e información personal de manera segura y sin mayor
  intervención una vez lo haya iniciado. La eliminación puede
  'limpiar' sus datos al eliminarlos para que no puedan ser
  recuperados por especialistas.
- Un nuevo menú de herramientas (para los comandos de Borra-Todo,
  claves, y oficinas centrales)
- Se mejoró la comunicación con los servidores Martus y los
  mensajes de estado del servidor se clarificaron.
- Hay más idiomas listados en los boletines.
- Se arregló un defecto en que versiones previas de Martus no
  podían bajar boletines de un servidor si es que el boletín
  contenía información pública con letras que no son inglesas
  (como alfabetos cirílicos o letras acentuadas). Este defecto no
  comprometía la habilidad de Martus para guardar boletines seguros
  a un servidor. El defecto solo era notorio si crea un boletín con
  información pública que contenga estas letras no-inglesas, lo
  suba a un servidor, borre el boletín sellado de su ordenador y
  luego intente bajarlo del servidor. El mensaje que vería al
  intentar esto es "Ocurrieron errores al bajar los resúmenes de
  los boletines. Algunos de los boletines en el servidor no podrán
  ser mostrados." Este defecto solo causó problemas con boletines
  sellados que tienen información pública, los boletines que son
  todos privados y los borradores no sufrieron de este mismo
  problema. Para poder bajar boletines con información pública en
  idiomas que no sean inglés es necesario usar un servidor de
  Martus versión 2.0 o más por lo que le recomendamos actualice su
  software si es necesario.
- El entorno de Java se a actualizado a la versión 1.4.2_03
- Numerosos defectos han sido arreglados, textos se han
  clarificado y ventanas del programa se han mejorado.


Q. Cambios de la versión 1.2 a la versión 1.2.1

- El programa se a traducido al ruso, incluyendo una versión
  especial del programa de instalación para usuarios rusos.
- Las guias de usuario en inglés y español han sido actualizadas
- El texto LinuxJavaInstall.txt ha sido creado para simplificar
  la instalación en ordenadores con el sistema operativo GNU/Linux.


R. Cambios de la versión 1.0.3 a la versión 1.2

- Ahora puede personalizar los campos de los boletines que crea
  en Martus. Esta característica solo es recomendada para usuarios
  avanzados. Se le advertirá de esto cuando intente usar esta
  característica, si no entiende lo que está a punto de hacer
  no continúe. Si decide continuar se le presentará una lista de
  los campos que puede quitar, cambiar de secuencia o agregar sus
  campos personalizados. Puede remover todos los campos a excepción
  de los cuatro originales: entrydate, language, author, y title.
  Cada campo personalizado tiene una "etiqueta", esto es una
  palabra toda en minúsculas, seguido de una coma y luego el título
  del campo cuando se muestra en la pantalla. El título puede tener
  mayúsculas, minúsculas y espacios. Si intenta guardar un campo
  personalizado que viola una de las reglas de los campos se le
  informará que hubo un problema pero no le indicará más
  específicamente.
- Ahora tiene la opción de ingresar un rango de fechas al indicar
  una fecha de un acontecimiento.
- El 90% del programa a sido traducido al ruso.
- Ahora puede exportar los contenidos de una carpeta.
- Si los puertos que Martus usa para comunicarse con un servidor
  no pueden ser usados por un contrafuegos (firewall), el programa
  usará los puertos estándar 80 y 443.
- El proceso para crear copias de reserva de sus claves ha sido
  simplificado y mejorado.
- El CD y la instalación de Martus en nuestro sitio web ahora
  incluye el entorno Java versión 1.4.1_03.
- Numerosos defectos han sido arreglados, textos se han
  clarificado, ventanas del programa se han mejorado y el uso del
  programa sin un mouse se a mejorado.


S. Cambios de la versión 1.0.2 a la versión 1.0.3

- Se han preparado versiones para sistema operativo Linux y Mac OS.
- Si es que a instalado una versión anterior de Martus ahora puede
  bajar del Internet una versión más pequeña sin tener que bajar
  el programa de instalación entero.
- Boletines pueden ser exportados en formato XML.
- La versión de Java es verificada durante la instalación y al
  iniciar el programa.
- Durante operaciones largas el cursor cambia a un reloj.
- Si usa Windows podrá ver archivos adjuntos directamente.
- Cambios a la licencia compatible con el GNU GPL, los
  requerimientos de notificación se han relajado si es que usa
  código de Martus en programas no relacionados.
- Ahora puede recibir mensajes de un servidor Martus, incluyendo
  notificaciones de conformidad.
- Ahora hay una nueva ventana al inicio indicando si el programa
  es una versión oficial de Martus.
- El programa de instalación se a simplificado y el programa de
  verificación puede bajarse de nuestro sitio Internet
  independientemente.
- El nuevo comando Borra-Todo elimina todas las copias de sus
  boletines de su disco duro.
- Se arreglo un defecto bajo el cual un boletín podría dañarse si
  lo baja de un servidor y luego lo modifica al agregarle un nuevo
  archivo adjunto.
- La traducción al español se a actualizado.
- Se arreglo el problema en donde la ventana donde ingresa su
  contraseña pasa al segundo plano cuando el programa descansa.
- La ventana de inicio de Martus siempre esta en primer plano y
  figura en la barra de Windows.
- El programa no es visible cuando esta editando un boletín.
- Las bibliotecas de criptografía han sido actualizadas al igual
  que la versión del entorno Java, v 1.4.1_02, el cual acepta
  letras en otros idiomas usando las teclas numéricas y también
  arregla un problema con escapes de memoria.
- Se agregó combinaciones de teclas estándares como del, y
  control-C, X, V, y A.


T. Cambios de la versión 1.0.1 a la versión 1.0.2

- Cuando intenta modificar un boletín, Martus crea una copia de
  este pero si el boletín original contiene archivos adjuntos
  el programa se confundía y dañaba la copia o el original o
  ambos. Este defecto se a solucionado.
- Ahora el campo de detalles de los boletines puede contener
  información predeterminada con el uso de la información
  contenida en un archivo llamado DefaultDetails.txt. Este
  archivo se puede incluir en CDs y el programa de instalación
  lo copia con el programa Martus y así este lo puede usar al
  crear una cuenta nueva.
- El código se a reorganizado para mantenerlo con mayor facilidad.


U. Cambios de la versión 1.0 a la versión 1.0.1

- El programa de instalación pregunta en Windows si es que
  quiere enlaces a Martus, la documentación y al programa de
  desinstalación en su menú Inicio.
- Se actualizó el entorno Java de la versión 1.4.1 a la versión
  1.4.1_01.
- Durante la instalación todo el código del programa, las
  librerías de terceros y la documentación es copiada a su
  disco duro.
- Errores en la ventana 'Acerca de Martus' y en la
  documentación fueron corregidos.


V. Instrucciones especiales si necesita actualizar Winsock al
  intentar ejecutar Martus bajo Windows 95:

Java requiere de la biblioteca Microsoft Winsock 2.0.  La gran
mayoría de instalaciones de Windows ya poseen esta librería.
Es posible que versiones de Windows 95 tengan una versión de
Winsock anterior.

Para verificar que versión de Winsock tiene instalada busque

el archivo "winsock.dll".
Abra las propiedades del archivo a través del menú 'Archivo' y
verifique la versión.

Si su sistema no tiene la versión 2.0 o mayor de Winsock,
utilice el programa de instalación que esta en el CD bajo la
carpeta Martus\Win95.

El siguiente sitio de web contiene información sobre los
componentes de Winsock bajo Windows 95 y su uso:

http://support.microsoft.com/support/kb/articles/Q177/7/19.asp

