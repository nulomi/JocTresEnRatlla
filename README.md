# Android-MVVM-TicTacToe

Pràctica de desenvolupament d'un **Tres en Ratlla (Tic-Tac-Toe)** amb Jetpack
Compose i arquitectura **MVVM amb model separat**.

**Package:** `org.proven.projecte.nuria1.joctresenratlla`

---

## 1. Anàlisi i documentació de l'arquitectura MVVM

### 1.1. Documentació dels components (basat en MVVMSimple)

**Model**
El Model és com un "motlle" de dades. A MVVMSimple és `SimpleState`, que només
guarda el número del comptador i un text. La seva funció principal és definir
quines dades necessita la interfície i com són: no fa càlculs ni decisions, només
és un contenidor de dades.

**ViewModel**
El ViewModel és el "cervell". A `SimpleViewModel` guarda un `_state` privat
(modificable) i exposa un `state` públic (només lectura) perquè la interfície el
consulti. Quan l'usuari prem un botó, la interfície crida el mètode
`incrementCounter()` del ViewModel, i aquest calcula el nou estat i actualitza
`_state`. El ViewModel no coneix la interfície: només diu "les dades han canviat"
i la interfície es redibuixa sola.

**View (UI / Compose)**
La interfície només és responsable de mostrar dades i rebre clics. A
`SimpleScreen` llegeix directament `viewModel.state.value` per mostrar el text, i
quan el botó es prem crida `viewModel.incrementCounter()`. La interfície no
modifica les dades ella mateixa, només "informa" el ViewModel.

### 1.2. Anàlisi comparatiu (MVVMSimple vs MVVM-separatModel)

**Estructura i separació de responsabilitats**

- A MVVMSimple el Model és només un `data class` (`SimpleState`) sense lògica.
  A MVVM-separatModel està dividit en `CardModel` i `BoardModel`, i ja té mètodes
  propis i un mètode de fàbrica `createInitialBoard()`.
- La lògica de negoci del joc (comprovar combinacions, girar cartes, etc.) a
  MVVMSimple s'executa al **ViewModel**. A MVVM-separatModel s'executa al **Model**
  (`BoardModel.onCardClick()`, `checkMatches()`...); el ViewModel només els crida.

**Immutabilitat i gestió de l'estat**

A MVVM-separatModel les dades del tauler no es modifiquen mai: es crea un objecte
nou amb `.copy()`. És important per a l'arquitectura reactiva perquè genera una
referència nova i Compose detecta el canvi i redibuixa. Si es mutés la llista
original, la UI no s'actualitzaria.

**Avantatges de la separació**

Desconnectar la lògica de negoci (`BoardModel`) del ViewModel és millor a mesura
que l'aplicació creix perquè: el ViewModel no es fa gros, es pot provar la lògica
sense la interfície, cada classe té una sola responsabilitat i, si hi ha un error
en les regles, ja saps on mirar.

### 1.3. Anàlisi comparatiu: `mutableStateOf` / `State<T>` vs `MutableStateFlow` / `StateFlow`

- **`mutableStateOf` / `State<T>`**: és l'estat natiu de Compose. Quan canvia el
  valor, Compose redibuixa sol les funcions que el llegeixen. És simple i va bé
  per a estats petits.
- **`MutableStateFlow` / `StateFlow`**: és un flux de coroutines de Kotlin. Pot
  emetre valors de manera asíncrona, admet diversos subscriptors i és segur entre
  fils. A Compose cal convertir-lo amb `collectAsState()`.

| Característica | `mutableStateOf` | `MutableStateFlow` |
|---|---|---|
| De qui és | Compose | Coroutines de Kotlin |
| Com es llegeix a Compose | `.value` directe | `collectAsState()` |
| Segur entre fils | No gaire | Sí |
| Diversos subscriptors | No | Sí |
| Operadors (`map`, `filter`...) | No | Sí |

**Quin escolliria i per què?** Tria `MutableStateFlow`, perquè la pràctica ho
demana (`MutableStateFlow` privat + `asStateFlow()` públic) i perquè és més
adequat per a un joc que pot ser observat per diversos components. A més, és el
que recomana Android per al ViewModel.

---

## 2. Arquitectura utilitzada en el Tres en Ratlla

El projecte aplica **MVVM amb model separat**:

### Model (lògica pura, immutable)
- **`CellModel`**: representa una casella (`id`, `position`, `symbol` "X"/"O"/"").
  Inclou `isEmpty()` i `withSymbol()`.
- **`BoardModel`**: conté les 9 caselles, el torn actual (`currentPlayer`), el
  guanyador (`winner`) i si el joc ha acabat (`isGameOver`).
    - `createInitialBoard()`: mètode de fàbrica que crea el tauler buit.
    - `makeMove(position)`: lògica pura que col·loca el símbol, comprova victòria/
      empat i retorna una **nova instància** amb `.copy()`.
    - `checkWinner()`: privat; comprova les 8 combinacions guanyadores.

### ViewModel (orquestració + estat reactiu)
- **`GameViewModel`**:
    - `_board: MutableStateFlow<BoardModel>` **privat**.
    - `board: StateFlow<BoardModel>` **públic** via `asStateFlow()`.
    - `onCellClick(position)`: delega al Model.
    - `restartGame()`: crea un tauler nou.

### View (Jetpack Compose)
- **`GameScreen`**: llegeix l'estat amb `collectAsState()`, mostra el torn actual,
  el missatge de victòria/empat i el botó de reinici.
- **`BoardGrid`**: tauler 3x3 amb `LazyVerticalGrid(GridCells.Fixed(3))`.
- **`CellItemView`**: cada casella, clicable si està buida i el joc no ha acabat.

### Captura de l'app en funcionament

_(Substitueix aquest text per la captura o GIF.)_
![Captura del joc](docs/screenshot.png)

---

## 3. Conceptes i mètodes apresos

- **`MutableStateFlow`**: contenidor observable d'estat. Es modifica amb
  `.value = ...`. És privat perquè només el ViewModel el pot canviar.
  `private val _board = MutableStateFlow(BoardModel.createInitialBoard())`

- **`asStateFlow()`**: exposa el `MutableStateFlow` com a `StateFlow` de només
  lectura, perquè la View no pugui modificar-lo directament.
  `val board: StateFlow<BoardModel> = _board.asStateFlow()`

- **`collectAsState()`**: converteix un `StateFlow` en un `State` de Compose.
  Quan el flux emet un valor nou, la `@Composable` que el llegeix es re-executa.
  `val board by viewModel.board.collectAsState()`

- **`.copy()`**: crea una còpia d'una `data class` canviant només els camps
  indicats. És la base de la immutabilitat i de l'arquitectura reactiva.
  `return this.copy(cards = updatedCards, currentPlayer = nextPlayer)`

- **`LazyVerticalGrid`**: graella que només compon els elements visibles. Més
  eficient que `Column`+`Row` per a llistes llargues.
  `LazyVerticalGrid(columns = GridCells.Fixed(3)) { items(...) { ... } }`

- **`viewModels()`**: delegat que crea/recupera el ViewModel lligat al cicle de
  vida de l'Activity. Sobrevviu a rotacions de pantalla.
  `private val viewModel: GameViewModel by viewModels()`

- **`aspectRatio(1f)`**: força que un composable sigui quadrat.
  `Modifier.aspectRatio(1f)`

- **`clickable(enabled = ...)`**: fa que un composable respongui a clics només si
  `enabled` és `true`.
  `Modifier.clickable(enabled = enabled) { onClick() }`

---