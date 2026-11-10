package org.proven.projecte.nuria1.joctresenratlla

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * VIEWMODEL: gestiona l'estat de la UI i orquestra el Model.
 *
 * IMPORTANT: el ViewModel NO conté lògica de negoci.
 * Només:
 *  - guarda l'estat actual en un MutableStateFlow privat
 *  - l'exposa com a StateFlow públic de només lectura
 *  - crida els mètodes del BoardModel quan l'usuari interactua
 *
 * Hereta de ViewModel (androidx.lifecycle.ViewModel) perquè sobrevisqui
 * a rotacions de pantalla i canvis de configuració.
 */
class GameViewModel : ViewModel() {

    /**
     * MutableStateFlow: contenidor observable d'estat.
     *
     * - "Mutable" → el podem modificar amb .value = ...
     * - "StateFlow" → emet valors de manera asíncrona i admet subscriptors
     * - És PRIVAT (_board) perquè NOMÉS el ViewModel ha de poder-lo canviar.
     *
     * El valor inicial és un tauler buit creat pel mètode de fàbrica.
     */
    private val _board = MutableStateFlow(BoardModel.createInitialBoard())

    /**
     * asStateFlow(): converteix el MutableStateFlow en un StateFlow
     * de NOMÉS LECTURA.
     *
     * Així la View pot observar-lo (collectAsState) però NO pot fer
     * _board.value = ... (ni tan sols compilaria, perquè no té el setter).
     *
     * Això garanteix el flux unidireccional de dades (UDF):
     *   View → ViewModel → Model → ViewModel → View
     */
    val board: StateFlow<BoardModel> = _board.asStateFlow()

    /**
     * Quan l'usuari toca una casella, deleguem la lògica al Model.
     * El Model retorna un NOU BoardModel (immutable) i nosaltres
     * l'assignem al flux → StateFlow emet → Compose redibuixa.
     */
    fun onCellClick(position: Int) {
        _board.value = _board.value.makeMove(position)
    }

    /**
     * Reinicia la partida creant un tauler nou amb createInitialBoard().
     * Torna a començar amb el torn de "X" i totes les caselles buides.
     */
    fun restartGame() {
        _board.value = BoardModel.createInitialBoard()
    }
}