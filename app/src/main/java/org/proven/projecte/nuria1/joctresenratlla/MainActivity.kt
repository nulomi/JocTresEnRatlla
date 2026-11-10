package org.proven.projecte.nuria1.joctresenratlla

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier

/**
 * ACTIVITY PRINCIPAL de l'aplicació.
 *
 * El seu únic paper és:
 *  - crear/recuperar el GameViewModel
 *  - muntar el Scaffold amb MaterialTheme
 *  - cridar GameScreen passant-li el ViewModel
 *
 * Tota la lògica viu al Model i al ViewModel; aquí no hi ha res de joc.
 */
class MainActivity : ComponentActivity() {

    /**
     * by viewModels(): delegat de l'Activity que crea/recupera el ViewModel
     * lligat al cicle de vida de l'Activity.
     *
     * Avantatges:
     *  - Sobrevviu a rotacions de pantalla (no es perd la partida).
     *  - Android s'encarrega de la seva creació i destrucció.
     *  - No cal fer 'GameViewModel()' manualment.
     */
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * enableEdgeToEdge(): fa que l'app ocupi tota la pantalla,
         * incloent-hi les zones de la barra d'estat i navegació.
         * Amb Scaffold + innerPadding gestionem els marges correctament.
         */
        enableEdgeToEdge()

        setContent {
            /**
             * MaterialTheme: aplica els colors, tipografies i formes
             * per defecte de Material 3 a tots els composables fills.
             * És el "tema" de l'app.
             */
            MaterialTheme {
                /**
                 * Scaffold: estructura bàsica de pantalla Material 3.
                 * Ens dona un 'innerPadding' que hem d'aplicar perquè
                 * el contingut no quedi tapat per les barres del sistema.
                 */
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}