// AlfonsoJose.kt
import java.io.File
import java.util.PriorityQueue
import kotlin.system.exitProcess

interface Grafo<T> {
    fun agregarVertice(v: T): Boolean
    fun conectar(desde: T, hasta: T): Boolean
    fun obtenerArcosSalida(v: T): List<T>
}

/**
 * Implementacion de un grafo dirigido utilizando listas de adyacencia.
 * Cada vertice almacena una lista de sus arcos de salida.
 *
 * @param T tipo generico de los vertices almacenados en el grafo
 */
class ListaAdyacenciaGrafo<T> : Grafo<T> {

    /**
     * Mapa interno que almacena cada vertice junto con su lista
     * de vertices adyacentes (arcos de salida).
     */
    private val adyacencia = mutableMapOf<T, MutableList<T>>()

    /**
     * Agrega un nuevo vertice al grafo si no existe previamente.
     *
     * @param v vertice a agregar
     * @return true si el vertice fue agregado, false si ya existia
     */
    override fun agregarVertice(v: T): Boolean {
        if (v in adyacencia) return false
        adyacencia[v] = mutableListOf()
        return true
    }

    /**
     * Crea un arco dirigido desde un vertice hacia otro.
     *
     * @param desde vertice origen
     * @param hasta vertice destino
     * @return true si ambos vertices existen y el arco fue creado
     */
    override fun conectar(desde: T, hasta: T): Boolean {
        if (desde !in adyacencia || hasta !in adyacencia) return false
        adyacencia[desde]!!.add(hasta)
        return true
    }

    /**
     * Devuelve la lista de arcos de salida de un vertice.
     * Se retorna una copia inmutable para evitar modificaciones externas.
     *
     * @param v vertice a consultar
     * @return lista de vertices adyacentes o una lista vacia
     */
    override fun obtenerArcosSalida(v: T): List<T> {
        return adyacencia[v]?.toList() ?: emptyList()
    }
}

/**
 * Clase que resuelve el problema de la Ciudad de Alfonso José.
 *
 * Esta clase implementa:
 *  - Lectura de la matriz de alturas desde archivo.
 *  - Construcción de un grafo donde cada celda es un vértice conectado a sus vecinos.
 *  - Cálculo del nivel mínimo de escape usando un Dijkstra modificado basado en cuellos de botella.
 *  - Cálculo del agua atrapada comparando alturas reales con niveles de escape.
 *
 * El objetivo es determinar cuánta agua queda retenida en la ciudad.
 */
class AlfonsoJose {

    /**
     * Representa una celda de la matriz mediante sus coordenadas (i, j).
     */
    data class Celda(val i: Int, val j: Int)

    /**
     * Lee una matriz de enteros desde un archivo de texto.
     *
     * Cada línea del archivo contiene números separados por espacios.
     *
     * @param nombre nombre del archivo a leer
     * @return matriz de enteros representada como Array<IntArray>
     */
    fun leerMatriz(nombre: String): Array<IntArray> {
        val lineas = File(nombre).readLines()                       // leer todas las líneas
        val matriz = Array(lineas.size) { IntArray(0) }             // matriz vacía

        var columnasEsperadas = -1                                  // para validar consistencia

        for ((idx, linea) in lineas.withIndex()) {
            if (linea.isBlank()) {                                    // línea vacía
                println("Línea vacía en el archivo en la fila $idx")
                exitProcess(1)
            }
            val tokens = linea.trim().split(" ")                    // separar por espacios

            if (columnasEsperadas == -1) {                           // primera fila
                columnasEsperadas = tokens.size                     // guardar cantidad de columnas
            } else if (tokens.size != columnasEsperadas) {             // filas irregulares
                println("Fila $idx tiene ${tokens.size} columnas, se esperaban $columnasEsperadas")
                exitProcess(1)
            }
            val nums = tokens.map { token ->                        // validar entero
                token.toIntOrNull() ?: run {
                    println("Valor no numérico '$token' en la fila $idx")
                    exitProcess(1)
                    }
                }  // validar entero

            matriz[idx] = nums.toIntArray()                         // guardar fila convertida
        }
        return matriz
    }

    /**
     * Construye un grafo a partir de la matriz de alturas.
     *
     * Cada celda se convierte en un vértice, y se conectan aristas bidireccionales
     * entre celdas adyacentes (arriba, abajo, izquierda, derecha).
     *
     * @param matriz matriz de alturas
     * @return un par (grafo, mapaDeAlturas)
     */
    fun construirGrafo(matriz: Array<IntArray>): Pair<Grafo<Celda>, Map<Celda, Int>> {

        val n = matriz.size                  // número de filas
        val m = matriz[0].size               // número de columnas

        val grafo = ListaAdyacenciaGrafo<Celda>()   // grafo vacío
        val alturas = mutableMapOf<Celda, Int>()    // mapa celda → altura

        // Crear vértices
        for (i in 0 until n) {               // recorrer filas
            for (j in 0 until m) {           // recorrer columnas
                val c = Celda(i, j)          // crear celda (i,j)
                grafo.agregarVertice(c)      // agregar vértice al grafo
                alturas[c] = matriz[i][j]    // guardar altura de la celda
            }
        }

        // Crear aristas entre celdas adyacentes
        val direcciones = listOf(            // desplazamientos a vecinos
            Pair(1, 0), Pair(-1, 0),         // arriba / abajo
            Pair(0, 1), Pair(0, -1)          // derecha / izquierda
        )

        for (i in 0 until n) {               // recorrer filas
            for (j in 0 until m) {           // recorrer columnas
                val u = Celda(i, j)          // celda actual
                for ((di, dj) in direcciones) {   // revisar cada dirección
                    val ni = i + di          // fila del vecino
                    val nj = j + dj          // columna del vecino
                    if (ni in 0 until n && nj in 0 until m) {   // dentro de límites
                        val v = Celda(ni, nj)   // celda vecina
                        grafo.conectar(u, v)    // conectar u → v
                        grafo.conectar(v, u)    // conectar v → u (bidireccional)
                    }
                }
            }
        }

        return Pair(grafo, alturas)          // devolver grafo y alturas
    }

    /**
     * Calcula el nivel mínimo de escape para cada celda usando un Dijkstra modificado.
     *
     * La idea es que el "costo" de un camino no es la suma de pesos,
     * sino el máximo de las alturas encontradas en el camino (cuello de botella).
     *
     * Las celdas del borde se inicializan con su propia altura,
     * ya que desde ellas el agua puede escapar directamente.
     *
     * @param grafo grafo de celdas
     * @param alturas mapa que asocia cada celda con su altura
     * @return mapa celda → nivel mínimo de escape
     */
    fun calcularNivelesEscape(grafo: Grafo<Celda>, alturas: Map<Celda, Int>): Map<Celda, Int> {

        val dist = mutableMapOf<Celda, Int>()                       // mapa de distancias (nivel mínimo de escape)
        val pq = PriorityQueue(compareBy<Pair<Celda, Int>> { it.second })   // cola de prioridad ordenada por nivel

        // Inicializar distancias
        for (v in alturas.keys) {                                   // recorrer todas las celdas
            dist[v] = Int.MAX_VALUE                                 // distancia inicial infinita
        }

        // Insertar todas las celdas del borde con su propia altura
        for (v in alturas.keys) {                                   // recorrer todas las celdas
            if (esBorde(v, alturas)) {                              // si la celda está en el borde
                dist[v] = alturas[v]!!                              // su nivel de escape es su altura
                pq.add(Pair(v, dist[v]!!))                          // agregar a la cola de prioridad
            }
        }

        // Dijkstra modificado
        while (pq.isNotEmpty()) {                                   // mientras haya nodos por procesar
            val (u, du) = pq.poll()                                 // extraer celda con menor nivel actual
            if (du > dist[u]!!) continue                            // descartar si ya fue mejorada

            for (v in grafo.obtenerArcosSalida(u)) {                // recorrer vecinos de u
                val alturaU = alturas[u]!!                          // altura de u
                val alturaV = alturas[v]!!                          // altura de v

                // El cuello de botella es el máximo entre:
                // - el cuello de botella previo
                // - la altura del vecino
                val nuevaDist = maxOf(du, alturaV)                  // calcular nuevo nivel de escape

                if (nuevaDist < dist[v]!!) {                        // si encontramos un mejor nivel
                    dist[v] = nuevaDist                             // actualizar distancia
                    pq.add(Pair(v, nuevaDist))                      // agregar a la cola
                }
            }
        }
        return dist                                                 // devolver niveles mínimos de escape
    }

    /**
     * Determina si una celda está en el borde de la matriz.
     *
     * @param c celda a evaluar
     * @param alturas mapa de alturas para inferir dimensiones
     * @return true si la celda está en el borde
     */
    fun esBorde(c: Celda, alturas: Map<Celda, Int>): Boolean {
        val n = alturas.keys.maxOf { it.i } + 1      // número total de filas (máximo índice i + 1)
        val m = alturas.keys.maxOf { it.j } + 1      // número total de columnas (máximo índice j + 1)
        return c.i == 0 ||                           // está en la primera fila
               c.j == 0 ||                           // está en la primera columna
               c.i == n - 1 ||                       // está en la última fila
               c.j == m - 1                          // está en la última columna
    }

    /**
     * Calcula el total de agua atrapada en la ciudad.
     *
     * Para cada celda:
     *  agua = nivelEscape - alturaReal
     * Si el valor es positivo, se acumula.
     *
     * @param alturas mapa celda → altura real
     * @param niveles mapa celda → nivel mínimo de escape
     * @return cantidad total de agua atrapada
     */
    fun calcularAgua(alturas: Map<Celda, Int>, niveles: Map<Celda, Int>): Int {
        var total = 0                                      // acumulador del agua total
        for (c in alturas.keys) {                          // recorrer todas las celdas
            val agua = niveles[c]!! - alturas[c]!!         // agua atrapada = nivel de escape - altura real
            if (agua > 0) total += agua                    // sumar solo si hay agua positiva
        }
        return total                                       // devolver el total acumulado
    }
}

/**
 * Punto de entrada del programa.
 *
 * Ejecuta el proceso completo:
 *  1. Lee la matriz desde archivo.
 *  2. Construye el grafo.
 *  3. Calcula niveles de escape.
 *  4. Calcula el agua atrapada.
 *  5. Imprime el resultado.
 */
fun main(args: Array<String>) {
    val solver = AlfonsoJose()
    val matriz = solver.leerMatriz("atlantis.txt")
    val (grafo, alturas) = solver.construirGrafo(matriz)
    val niveles = solver.calcularNivelesEscape(grafo, alturas)
    val agua = solver.calcularAgua(alturas, niveles)
    println(agua)
}
