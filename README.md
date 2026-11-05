# Kata 4 — Cliente HTTP

**Ingeniería del Software II · Universidad de Las Palmas de Gran Canaria**

## Objetivo

Desarrollar un cliente Java que consuma un servicio HTTP, interprete su respuesta JSON y muestre los resultados por consola.

Para esta kata se utiliza un servicio local que devuelve los primeros términos de la serie de Fibonacci. El cliente consulta el endpoint `/fibonacci`, transforma la respuesta en un objeto del modelo y presenta la serie obtenida.

La implementación separa la lógica de aplicación de los detalles de comunicación, transformación de datos y presentación mediante puertos y adaptadores.

## Tecnologías

- Java 21
- Maven
- Java HttpClient
- Gson 2.15.0
- JUnit 5.12.2

## Estructura del proyecto

```text
kata4/
├── pom.xml
├── README.md
├── .gitignore
├── tools/
│   └── FibonacciDemoServer.java
└── src/
    ├── main/java/software/ulpgc/katas/
    │   ├── architecture/
    │   │   ├── model/
    │   │   │   └── Series.java
    │   │   ├── domain/
    │   │   │   └── FetchFibonacciSeriesUseCase.java
    │   │   └── ports/
    │   │       ├── SeriesService.java
    │   │       ├── SeriesParser.java
    │   │       └── ServiceException.java
    │   └── infrastructure/
    │       ├── Main.java
    │       ├── adapters/
    │       │   ├── HttpSeriesService.java
    │       │   └── JsonSeriesParser.java
    │       └── console/
    │           └── ConsolePresenter.java
    └── test/java/
        └── software/ulpgc/katas/
            ├── architecture/domain/
            └── infrastructure/
                ├── adapters/
                └── console/
```

### Arquitectura

El proyecto distingue dos partes:

**Arquitectura**

Contiene el modelo, el caso de uso y los puertos de servicio.

- `Series`: representa los valores obtenidos mediante un modelo inmutable.
- `FetchFibonacciSeriesUseCase`: coordina la consulta y valida la cantidad de términos solicitados.
- `SeriesService`: define el contrato para obtener una serie.
- `SeriesParser`: define el contrato para transformar una respuesta en un objeto del modelo.
- `ServiceException`: distingue los errores de red, de estado HTTP y de formato de respuesta.

**Infraestructura**

Contiene las implementaciones relacionadas con las tecnologías utilizadas.

- `HttpSeriesService`: construye y ejecuta la petición HTTP.
- `JsonSeriesParser`: transforma el JSON recibido en un objeto `Series`.
- `ConsolePresenter`: prepara la salida que se muestra al usuario.
- `Main`: configura las dependencias y ejecuta el caso de uso.

El caso de uso depende de `SeriesService`, no de la implementación HTTP. Esto permite sustituir el adaptador sin modificar la lógica de aplicación.

El servidor incluido en `tools` se utiliza únicamente como apoyo para las pruebas manuales.

## Compilación y ejecución

Se necesita JDK 21 o superior y Maven.

Desde la raíz del proyecto:

```bash
mvn clean test
```

### 1. Iniciar el servicio

En una terminal:

```bash
java tools/FibonacciDemoServer.java
```

El servidor se inicia en el puerto `8081` y expone el endpoint:

```text
GET http://localhost:8081/fibonacci?n=8
```

Respuesta:

```json
{
  "values": [0, 1, 1, 2, 3, 5, 8, 13]
}
```

El servidor debe permanecer en ejecución mientras se utiliza el cliente.

### 2. Ejecutar el cliente

En otra terminal:

```bash
mvn compile exec:java "-Dexec.args=8 http://localhost:8081"
```

Salida esperada:

```text
Fibonacci (8 términos): 0, 1, 1, 2, 3, 5, 8, 13
```

Los argumentos son opcionales:

```text
[cantidad de términos] [URL base]
```

Si no se indican, se solicitan ocho términos al servicio local del puerto `8081`.

La cantidad admitida está comprendida entre 1 y 46 términos.

También se puede ejecutar `Main` desde IntelliJ IDEA, indicando los argumentos en la configuración de ejecución.

## Gestión de errores

El cliente distingue entre errores de conexión, respuestas HTTP de error y respuestas con un formato incorrecto.

El servidor auxiliar permite reproducir estos casos.

**Servicio no disponible (HTTP 503)**

```bash
java tools/FibonacciDemoServer.java 8082 error
```

Ejecutando el cliente contra `http://localhost:8082` se obtiene:

```text
El servicio respondió con HTTP 503.
```

**Respuesta JSON incorrecta**

```bash
java tools/FibonacciDemoServer.java 8082 malformed
```

El cliente detecta que la respuesta no tiene un formato válido.

**Respuesta vacía**

```bash
java tools/FibonacciDemoServer.java 8082 empty
```

El cliente identifica la respuesta vacía como inválida.

**Error de conexión**

También se puede probar el cliente contra un puerto sin ningún servidor en ejecución. En ese caso se informa de que no ha sido posible establecer la conexión.

Para cada prueba se utiliza una instancia del servidor con el modo correspondiente.

## Pruebas

Las pruebas automatizadas están desarrolladas con JUnit 5 y se ejecutan mediante:

```bash
mvn clean test
```

Se comprueban los siguientes comportamientos:

- Ejecución del caso de uso mediante su puerto de servicio.
- Validación de la cantidad de términos solicitados.
- Transformación de respuestas JSON correctas.
- Rechazo de respuestas vacías o con datos inválidos.
- Tratamiento de códigos HTTP de error y problemas de conexión.
- Formato de presentación de resultados y errores.

Las pruebas del cliente HTTP utilizan un servidor local de prueba y no requieren que el servidor de demostración esté iniciado.

## Repeticiones

La kata se ha realizado tres veces, manteniendo el mismo objetivo y la misma separación de responsabilidades.

| Repetición | Rama | Ejecución de comprobación |
|---|---|---|
| 1 | `repetition/1` | 5 términos |
| 2 | `repetition/2` | 6 términos |
| 3 | `repetition/3` | 8 términos |

Cada repetición parte del mismo esqueleto y vuelve a implementar el adaptador HTTP, el parser, la presentación y la composición de la aplicación.

El historial de Git conserva los commits de cada repetición. Las versiones están identificadas mediante los tags `kata4-r1`, `kata4-r2` y `kata4-r3`.

La rama `main` contiene la versión final de la entrega.