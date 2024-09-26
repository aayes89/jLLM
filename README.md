# jLLM
Un modelo de lenguaje n-gramas para Java.<br>
El entrenamiento fue realizado utilizando como conjunto de datos:
* <b>La Biblia</b> (Reina Valera), edición 1960
* <b>Diccionario de la RAE</b> (Real Academia Española), edición de 2023
* <b>Novelas ligeras variadas</b>, en Español
* <b>El Evangelio segun Marcos</b>
* <b>El Quijote</b>, de Miguel de Cervantes Saavedra
* <b>Coriolano</b> (Tragedia de William Shakespare), en Inglés.

# ¿Qué hace?
* Genera textos basado en los conjuntos de datos.
* Comprime el modelo utilizando formato ZIP compatible para disminuir tamaño.
* Carga un modelo existente (creado por la aplicación).
* Guarda el modelo entrenado en un fichero comprimido compatible con formato ZIP.
* Evita redundancia en los conjuntos de datos (evita repetiición de información en el modelo).
* Manejo de errores y excepciones.

# ¿Qué tiene pendiente?
* <b>Aumentar entrenamiento:</b> incluir más conjuntos de datos en texto plano.
* <b>Mejorar:</b> el algoritmo de generación de textos.
* <b>Interfaz gráfica:</b> desarrollar una interfaz gráfica para facilitar el uso del programa a aquellos que gustan de esta funcionalidad existente en modo consola (texto).
* <b>Integración:</b> permitir la interacción con modelos grandes (LLM) existentes en caso de requerir utilizarlos.
* <b>Optimización:</b> depurar el código para hacerlo más limpio, legible y robusto.

# ¿Cómo usar?
* Descargue el modelo (model.dat) a cualquier directorio de su gusto.
* Cree un nuevo proyecto en NetBeans o su IDE Java de agrado.
* Copie y pegue el contenido de las clases en este repositorio y ajuste el paquete del proyecto al suyo sino al de las clases.
* Edite las líneas referentes a "/ruta/al/llm" por las del directorio que guste sino puede eliminarlas para que se cargue una ruta por defecto.
* Compile y ejecute.
  
# Información
Si gusta apoyar con el proyecto, no dude en dejar su comentario o abrir nuevas instancias para mejorar el proyecto.<br>
Cuando se ejecuta el programa, si no encuentra un modelo en el directorio de búsqueda, deja la opción de generar uno nuevo con datos previos.

# Agradecimientos
A mi esposa: por ser tolerante con el tiempo que he dedicado a realizar este proyecto. 

