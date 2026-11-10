package org.proven.projecte.nuria1.joctresenratlla

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * PANTALLA PRINCIPAL del Tres en Ratlla.
 *
 * Responsabilitats:
 *  - Observar l'estat reactiu del ViewModel amb collectAsState()
 *  - Mostrar el torn actual
 *  - Mostrar el missatge de victòria/empat + botó reinici
 *  - Dibuixar el tauler 3x3
 *
 * @param viewModel el ViewModel que conté l'estat del joc (StateFlow<BoardModel>).
 * @param modifier  modificador per aplicar padding des de fora (MainActivity).
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    /**
     * collectAsState(): converteix un StateFlow en un State de Compose.
     *
     * Quan el StateFlow emet un valor nou (perquè el ViewModel ha assignat
     * un BoardModel nou al _board), Compose detecta el canvi i re-executa
     * aquesta @Composable i totes les que llegeixen 'board'.
     *
     * 'by' (delegate) ens permet escriure 'board.isGameOver' en lloc de
     * 'board.value.isGameOver'.
     */
    val board by viewModel.board.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ---- Indicador del torn actual (només mentre el joc no ha acabat) ----
        if (!board.isGameOver) {
            Text(
                text = "Torn de: ${board.currentPlayer}",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ---- Missatge de victòria/empat + botó reinici ----
        if (board.isGameOver) {
            val message = when (board.winner) {
                "EMPAT" -> "Empat! 🤝"
                else -> "Guanyador: ${board.winner} 🎉"
            }
            Text(
                text = message,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(onClick = { viewModel.restartGame() }) {
                Text(text = "Tornar a jugar")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ---- Tauler 3x3 ----
        BoardGrid(
            board = board,
            onCellClick = { position -> viewModel.onCellClick(position) }
        )
    }
}

/**
 * TAULER 3x3 amb LazyVerticalGrid.
 *
 * LazyVerticalGrid: graella que NOMÉS compon (pinta) els elements visibles.
 * Amb 9 elements la diferència amb Column+Row és mínima, però és el que
 * demana la pràctica i és el patró recomanat per a llistes/graelles.
 *
 * Paràmetres:
 *  - columns = GridCells.Fixed(3): sempre 3 columnes → tauler 3x3
 *  - contentPadding: espai intern de la graella
 *  - horizontalArrangement / verticalArrangement: espai entre cel·les (8.dp)
 *
 * 'items' recorre la llista de caselles i pinta una CellItemView per a cada una.
 * 'key = { it.id }' ajuda Compose a identificar cada element quan es redibuixa.
 */
@Composable
fun BoardGrid(
    board: BoardModel,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(board.cards, key = { it.id }) { cell ->
            CellItemView(
                cell = cell,
                enabled = !board.isGameOver && cell.isEmpty(),
                onClick = { onCellClick(cell.position) }
            )
        }
    }
}

/**
 * VISUALITZACIÓ D'UNA CASELLA.
 *
 * Mostra el símbol (X/O) centrat. Si la casella està buida i el joc no ha
 * acabat, permet clicar-hi. Si està ocupada o el joc ha acabat, ignora el clic.
 *
 * @param cell     la casella a dibuixar (id, position, symbol)
 * @param enabled  true si es pot clicar (buida i joc actiu)
 * @param onClick  callable que s'executa en clicar
 */
@Composable
fun CellItemView(
    cell: CellModel,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            /**
             * aspectRatio(1f): força que el composable sigui QUADRAT
             * (amplada = alçada). Perfecte per a les cel·les del 3x3.
             */
            .aspectRatio(1f)
            /**
             * clickable(enabled = ...): fa que el Card respongui a clics
             * NOMÉS si 'enabled' és true. Si el joc ha acabat o la casella
             * està ocupada, el clic s'ignora automàticament.
             */
            .clickable(enabled = enabled) { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)), // fons clar per veure bé X i O
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = cell.symbol,
                style = MaterialTheme.typography.displayMedium,
                color = when (cell.symbol) {
                    "X" -> Color(0xFF1565C0) // blau
                    "O" -> Color(0xFFC62828) // vermell
                    else -> Color.Transparent
                }
            )
        }
    }
}