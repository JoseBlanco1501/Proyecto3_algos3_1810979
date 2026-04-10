# Proyecto3_algos3_1810979: Mundo cubo

## Integrantes
- Jose Alejandro Blanco Rojas - Carnet 18-10979

---

## Instrucciones de ejecucion

### 1. Compilar el programa

- Escribir en la terminal

kotlinc AlfonsoJose.kt -include-runtime -d AlfonsoJose.jar

---

### 2. Ejecutar el programa

java -jar AlfonsoJose.jar

---

## 1. Introducción

El problema de la **Ciudad de Alfonso José** consiste en determinar cuánta agua queda atrapada en una ciudad representada por una matriz de alturas. Cada celda de la matriz es un bloque de terreno con una altura fija. El agua puede fluir hacia cualquier celda adyacente (arriba, abajo, izquierda, derecha) y eventualmente escapar por los bordes de la ciudad.

El objetivo es calcular, para cada celda:

- El **nivel mínimo de escape**, es decir, la altura mínima que debe alcanzar el agua para poder salir de la ciudad.
- La cantidad de **agua atrapada**, definida como:
  

$
\text{agua}(c) = \max(0,\ \text{nivelEscape}(c) - \text{altura}(c))
$



Para resolver este problema se implementa:

- Un **grafo dirigido** donde cada celda es un vértice.
- Conexiones bidireccionales entre celdas adyacentes.
- Un **Dijkstra modificado**, donde el costo de un camino no es la suma de pesos, sino el **máximo** de las alturas encontradas.
- Un cálculo final del agua atrapada.

---
##  Complejidad temporal y espacial de la implementación

La siguiente tabla resume la complejidad de cada operación implementada en el grafo `ListaAdyacenciaGrafo<T>` y en las funciones principales del algoritmo de Alfonso José.

| Operación / Función              | Complejidad | Justificación |
|----------------------------------|-------------|---------------|
| **agregarVertice**               | O(1)        | Insertar una clave en un `MutableMap` es O(1) promedio. Se crea una lista vacía para el vértice. |
| **conectar**                     | O(1)        | Agregar un elemento al final de una `MutableList` es O(1) amortizado. No se realizan búsquedas adicionales. |
| **obtenerArcosSalida**           | O(k)        | Se retorna una copia inmutable de la lista de adyacencia. Si el vértice tiene k vecinos, copiar la lista cuesta O(k). |
| **leerMatriz**                   | O(n·m)      | Se recorren todas las líneas y todos los elementos de la matriz. Cada conversión a entero es O(1). |
| **construirGrafo**               | O(n·m)      | Se crean n·m vértices y se conectan hasta 4 vecinos por celda. El número total de aristas es proporcional a n·m. |
| **esBorde**                      | O(1)        | Solo compara índices con los límites de la matriz. |
| **calcularNivelesEscape (Dijkstra modificado)** | O(E log V) = O(n·m log(n·m)) | Cada celda se inserta en la cola de prioridad. Cada arista se relaja una vez. La cola de prioridad domina el costo. |
| **calcularAgua**                 | O(n·m)      | Se recorre cada celda una vez y se realiza una resta y comparación. |
| **Espacio del grafo**            | O(V + E)    | Se almacena una lista por vértice (O(V)) y un nodo por cada arista (O(E)). |
| **Espacio total del algoritmo**  | O(n·m)      | Se almacenan: matriz, mapa de alturas, distancias, grafo y cola de prioridad. Todos proporcionales a n·m. |

---

### Resumen de magnitudes

- **V = n·m** vértices (una celda = un vértice).  
- **E ≈ 4·n·m** aristas (cada celda tiene hasta 4 vecinos).  
- **Complejidad total del algoritmo:**  

$
  O(nm \log(nm))
$

---
## Decisiones de Implementación

La solución al problema de la Ciudad de Alfonso José requiere combinar estructuras de datos eficientes, un modelo matemático adecuado y un algoritmo de búsqueda óptimo. A continuación se detallan las decisiones de diseño más importantes tomadas durante la implementación, junto con su justificación técnica.

---

### 1. Representación de la ciudad como un grafo

Cada celda de la matriz se modela como un **vértice** del grafo, y cada celda adyacente (arriba, abajo, izquierda, derecha) se conecta mediante aristas bidireccionales.

**Decisión:** usar una estructura de **lista de adyacencia** (`MutableMap<T, MutableList<T>>`).

**Justificación:**

- La matriz es densa y regular, pero cada celda solo tiene hasta 4 vecinos → el grafo es **esparsivo**.
- Las listas de adyacencia permiten:
  - Recorridos eficientes.
  - Inserción O(1) de vértices y aristas.
  - Bajo uso de memoria comparado con matrices de adyacencia.
- Facilita la implementación de algoritmos de búsqueda como Dijkstra.

---

### 2. Los pesos se almacenan en los **vértices**, no en las aristas

Este es el punto más importante del diseño.

En un grafo tradicional:

- Los pesos están en las aristas.
- El costo de un camino es la suma de los pesos.
- Dijkstra minimiza esa suma.

Pero en este problema:

- La altura pertenece a la **celda**, no al movimiento.
- El agua fluye libremente entre celdas.
- El costo de un camino no es la suma de alturas, sino el **máximo** encontrado en el recorrido.



$
\text{costo}(P) = \max_{v \in P} \text{altura}(v)
$



**Decisión:** almacenar la altura en el vértice y no en la arista.

**Justificación:**

- El cuello de botella depende del vértice más alto del camino.
- No existe un “costo de transición” entre celdas.
- El algoritmo debe propagar **máximos**, no sumas.
- Permite implementar un Dijkstra modificado sin redefinir estructuras de aristas.

---

### 3. Uso de un Dijkstra modificado basado en cuellos de botella

El objetivo es calcular el **nivel mínimo de escape** para cada celda:



$$
\text{nivelEscape}(c) = \min_{P} \left( \max_{v \in P} \text{altura}(v) \right)
$$



Esto no es un problema de distancias tradicionales.

**Decisión:** modificar Dijkstra para que la operación de relajación use `max` en lugar de suma.

#### Relajación clásica:
```kotlin
dist[v] = dist[u] + w(u,v)
```
#### Relajación modificada:
```kotlin
dist[v] = max(dist[u], altura(v))
```

**Justificación:**

- El cuello de botella de un camino es el máximo de las alturas visitadas.
- La operación `max` es monótona → Dijkstra sigue siendo correcto.
- La cola de prioridad garantiza que siempre se expande el vértice con el menor cuello de botella conocido.

---

### 4. Inicialización desde las celdas del borde

Las celdas del borde permiten que el agua escape directamente.

**Decisión:** inicializar su nivel de escape con su propia altura.

```kotlin
if (esBorde(v)) {
    dist[v] = alturas[v]!!
    pq.add(Pair(v, dist[v]!!))
}
```

**Justificación:**

- El agua no necesita subir para escapar desde el borde.
- Estas celdas actúan como fuentes en el algoritmo.
- Permite que el Dijkstra modificado propague niveles de escape hacia el interior.

---

### 5. Cálculo del agua atrapada

Una vez calculados los niveles de escape:

```kotlin
agua = niveles[c] - alturas[c]
```
**Decisión:** sumar solo valores positivos.

**Justificación:**

- Si el nivel de escape es menor o igual a la altura, no hay agua atrapada.
- La diferencia representa la altura de la columna de agua sobre la celda.
