# Proyecto3_algos3_1810979: Implementacion de un Grafo Dirigido con Listas de Adyacencia

## Integrantes
- Jose Alejandro Blanco Rojas - Carnet 18-10979

---

## Instrucciones de ejecucion

---

## 1. Introducción

El problema de la **Ciudad de Alfonso José** consiste en determinar cuánta agua queda atrapada en una ciudad representada por una matriz de alturas. Cada celda de la matriz es un bloque de terreno con una altura fija. El agua puede fluir hacia cualquier celda adyacente (arriba, abajo, izquierda, derecha) y eventualmente escapar por los bordes de la ciudad.

El objetivo es calcular, para cada celda:

- El **nivel mínimo de escape**, es decir, la altura mínima que debe alcanzar el agua para poder salir de la ciudad.
- La cantidad de **agua atrapada**, definida como:
  

\[
  \text{agua}(c) = \max(0,\ \text{nivelEscape}(c) - \text{altura}(c))
  \]


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

### ✔ Resumen de magnitudes

- **V = n·m** vértices (una celda = un vértice).  
- **E ≈ 4·n·m** aristas (cada celda tiene hasta 4 vecinos).  
- **Complejidad total del algoritmo:**  
  

\[
  O(nm \log(nm))
  \]



Este es el costo óptimo para este tipo de problema, equivalente a las soluciones más eficientes en literatura (similar al algoritmo de “Trapping Rain Water II”).



