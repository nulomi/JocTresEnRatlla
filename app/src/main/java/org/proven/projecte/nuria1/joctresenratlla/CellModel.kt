package org.proven.projecte.nuria1.joctresenratlla

/**
 * MODEL D'UNA CASELLA (Cell)
 *
 * Representa una de les 9 caselles del tauler 3x3.
 * És IMMUTABLE: cada canvi retorna una còpia nova amb .copy().
 *
 * @param id       identificador únic de la casella (0..8). S'usa com a 'key' al LazyVerticalGrid
 *                 perquè Compose sàpiga quina cel·la és quina quan es redibuixa.
 * @param position índex del 0 al 8 (0 = dalt-esquerra; 8 = baix-dreta).
 * @param symbol   "X", "O" o "" si està buida.
 */
data class CellModel(
    val id: Int,
    val position: Int,
    val symbol: String = ""
) {
    /** Retorna true si la casella està buida (ningú hi ha jugat encara). */
    fun isEmpty(): Boolean = symbol.isEmpty()

    /**
     * Retorna una CÒPIA d'aquesta casella amb un símbol nou.
     *
     * .copy() és un mètode que Kotlin genera automàticament per a les data class:
     * crea un objecte NOU canviant només els camps que li passes i mantenint la resta.
     * És la base de la immutabilitat: no modifiquem mai l'objecte original,
     * sinó que en creem un de nou. Això fa que Compose detecti el canvi i redibuixi.
     */
    fun withSymbol(newSymbol: String): CellModel = copy(symbol = newSymbol)
}