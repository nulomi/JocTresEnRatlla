package org.proven.projecte.nuria1.joctresenratlla

/**
 * MODEL DEL TAULER (lògica pura, sense dependències d'Android)
 *
 * Conté:
 *  - cards: les 9 caselles (CellModel)
 *  - currentPlayer: "X" o "O" → el torn actual
 *  - winner: "X", "O", "EMPAT" o null si encara no ha acabat
 *  - isGameOver: true quan algú guanya o s'omple el tauler
 *
 * Tots els mètodes són PURS: no modifiquen mai l'objecte actual,
 * sinó que retornen una NOVA instància amb .copy().
 * Això és el que fa que StateFlow detecti el canvi i Compose redibuixi.
 */
data class BoardModel(
    val cards: List<CellModel> = emptyList(),
    val currentPlayer: String = "X",
    val winner: String? = null,
    val isGameOver: Boolean = false
) {
    companion object {
        /**
         * MÈTODE DE FÀBRICA: crea el tauler inicial buit amb 9 caselles.
         * S'usa des del ViewModel per reiniciar la partida.
         * 'companion object' = espai on posem funcions/constants "d'estàtiques"
         * que no depenen d'una instància concreta de BoardModel.
         */
        fun createInitialBoard(): BoardModel {
            val initialCells = (0 until 9).map { index ->
                CellModel(id = index, position = index, symbol = "")
            }
            return BoardModel(cards = initialCells)
        }

        /**
         * Les 8 combinacions guanyadores del 3x3.
         * Cada subllista té 3 índexs de posició (0..8).
         * Si aquestes 3 caselles tenen el mateix símbol → algú ha guanyat.
         */
        private val WINNING_COMBINATIONS = listOf(
            listOf(0, 1, 2), // fila superior
            listOf(3, 4, 5), // fila del mig
            listOf(6, 7, 8), // fila inferior
            listOf(0, 3, 6), // columna esquerra
            listOf(1, 4, 7), // columna central
            listOf(2, 5, 8), // columna dreta
            listOf(0, 4, 8), // diagonal principal ↘
            listOf(2, 4, 6)  // diagonal secundària ↙
        )
    }

    /**
     * LÒGICA PURA DE MOVIMENT.
     *
     * Flux:
     *  1. Si el joc ja ha acabat → no fem res (retornem 'this').
     *  2. Busquem la casella clicada; si no existeix o ja té símbol → no fem res.
     *  3. Col·loquem el símbol del jugador actual a la casella.
     *  4. Comprovem si hi ha guanyador o si el tauler s'ha omplert (empat).
     *  5. Canviem de torn si el joc continua.
     *  6. Retornem una NOVA instància amb .copy() → immutabilitat.
     */
    fun makeMove(position: Int): BoardModel {
        // 1. Si el joc ha acabat, ignorem el clic
        if (isGameOver) return this

        // 2. Busquem la casella per posició
        val target = cards.firstOrNull { it.position == position } ?: return this
        // Si ja té símbol (X o O), el moviment és invàlid
        if (!target.isEmpty()) return this

        // 3. Actualitzem la casella amb el símbol del jugador actual
        val updatedCards = cards.map { cell ->
            if (cell.position == position) cell.withSymbol(currentPlayer) else cell
        }

        // 4. Comprovem si hi ha guanyador amb el nou estat
        val winningSymbol = checkWinner(updatedCards)
        val hasWinner = winningSymbol != null
        val isFull = updatedCards.all { !it.isEmpty() }
        val gameEnded = hasWinner || isFull

        // 5. Canviem de torn (només si el joc continua, si no és igual)
        val nextPlayer = when {
            gameEnded -> currentPlayer
            currentPlayer == "X" -> "O"
            else -> "X"
        }

        // Determinem el valor del camp 'winner'
        val winnerValue: String? = when {
            hasWinner -> winningSymbol
            isFull -> "EMPAT"
            else -> null
        }

        /**
         * .copy() → crea una CÒPIA d'aquest BoardModel canviant només
         * els camps indicats (cards, currentPlayer, winner, isGameOver)
         * i mantenint la resta igual.
         *
         * És CLAU: l'objecte original no es toca. StateFlow detecta
         * que la referència ha canviat (nova instància) i emet un valor nou,
         * i Compose redibuixa la UI.
         */
        return this.copy(
            cards = updatedCards,
            currentPlayer = nextPlayer,
            winner = winnerValue,
            isGameOver = gameEnded
        )
    }

    /**
     * COMPROVACIÓ DE VICTÒRIA (privada, només s'usa dins makeMove).
     *
     * Recorre les 8 combinacions guanyadores:
     *  - agafa els 3 símbols de cada combinació
     *  - si tots 3 són iguals i NO estan buits → guanyador
     * Retorna "X", "O" o null.
     */
    private fun checkWinner(currentCards: List<CellModel>): String? {
        for (combination in WINNING_COMBINATIONS) {
            val (a, b, c) = combination
            val symbolA = currentCards[a].symbol
            val symbolB = currentCards[b].symbol
            val symbolC = currentCards[c].symbol
            if (symbolA.isNotEmpty() && symbolA == symbolB && symbolB == symbolC) {
                return symbolA
            }
        }
        return null
    }
}