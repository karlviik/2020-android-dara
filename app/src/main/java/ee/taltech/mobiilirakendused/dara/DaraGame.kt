package ee.taltech.mobiilirakendused.dara

import android.util.Log
import kotlin.random.Random

class DaraGame {
	private var isSecondPlayerAi: Boolean
	private var isFirstPlayerAi: Boolean
	// game phase, 1 for drop, 2 for move phase, 3 if finished
	private var phase = 1
	// player pieces
	private var playerPieces = intArrayOf(0, 0)
	// storage for player chosen piece
	private var chosenPiece = intArrayOf(0, 0)
	// pieces to drop for drop phase
	private var toDropCount = 24
	// initial board, filled with 13s as red player starts and they can place everywhere
	private var board = Array(5) { IntArray(6) { 13 } }
	// 1 for drop, 2 for select your piece, 3 for select place to go or place back, 4 for choose opponent piece
	private var turnType = 1
	// whose turn is it, 1 for red, 2 for blue
	private var turn: Int = 1
	// winner
	private var winner: Int = 0
	// id
	private var id: Int = Random.nextInt()

	constructor(isFirstPlayerAi: Boolean, isSecondPlayerAi: Boolean) {
		this.isFirstPlayerAi = isFirstPlayerAi
		this.isSecondPlayerAi = isSecondPlayerAi
	}

	constructor(
		isSecondPlayerAi: Boolean,
		isFirstPlayerAi: Boolean,
		phase: Int,
		playerPieces: IntArray,
		chosenPiece: IntArray,
		toDropCount: Int,
		board: Array<IntArray>,
		turnType: Int,
		turn: Int,
		winner: Int,
		id: Int
	) {
		this.isSecondPlayerAi = isSecondPlayerAi
		this.isFirstPlayerAi = isFirstPlayerAi
		this.phase = phase
		this.playerPieces = playerPieces
		this.chosenPiece = chosenPiece
		this.toDropCount = toDropCount
		this.board = board
		this.turnType = turnType
		this.turn = turn
		this.winner = winner
		this.id = id
	}


	companion object {
		private val TAG = this::class.java.declaringClass!!.simpleName
		val TURNS = arrayOf(
			arrayOf(0, 1),
			arrayOf(1, 0),
			arrayOf(-1, 0),
			arrayOf(0, -1)
		)
	}

	fun doAiMove() {
		when (phase) {
			1 -> {
				val move = getAllValidDropMoves().random()
				insertMove(move[0], move[1])
			}
			2 -> {
				when (turnType) {
					2 -> {
						val pieces = getMovablePieces()
						val goodPieces = ArrayList<IntArray>()
						val badPieces = ArrayList<IntArray>()

						for (piece in pieces) {
							if (getThreeInRowMoves(
									piece[0],
									piece[1],
									getPotentialPieceMoves(piece[0], piece[1])
								).isNotEmpty()
							) {
								goodPieces.add(piece)
							}
							if (!isPartOfNInRow(3, piece[0], piece[1])) {
								badPieces.add(piece)
							}
						}
						val move: IntArray
						move = when {
							goodPieces.isNotEmpty() -> {
								goodPieces.random()
							}
							badPieces.isNotEmpty() -> {
								badPieces.random()
							}
							else -> {
								pieces.random()
							}
						}
						insertMove(move[0], move[1])
					}
					3 -> {
						// TODO this part wants fixing
						val potMoves = getAllValidPieceMoves(chosenPiece[0], chosenPiece[1])
						if (potMoves.isEmpty()) {
							Log.d(TAG, "I see no things")
							insertMove(chosenPiece[0], chosenPiece[1])
						}
						else {
							Log.d(TAG, "I see THINGS")
							val goodMoves =
								getThreeInRowMoves(chosenPiece[0], chosenPiece[1], potMoves)
							val move: IntArray
							move = if (goodMoves.isEmpty()) {
								potMoves.random()
							}
							else {
								goodMoves.random()
							}
							Log.d(TAG, "THe move should be " + move[0] + move[1])
							insertMove(move[0], move[1])
						}
					}
					4 -> {
						val potTakes = getAllValidTakeMoves()
						val betterTakes = ArrayList<IntArray>()
						for (take in potTakes) {
							if (isPartOfNInRow(2, take[0], take[1])) {
								betterTakes.add(take)
							}
						}
						val move = if (betterTakes.isNotEmpty()) {
							betterTakes.random()
						}
						else {
							potTakes.random()
						}
						insertMove(move[0], move[1])
					}
				}
			}
			3 -> return
		}
	}


	private fun getThreeInRowMoves(
		y: Int,
		x: Int,
		potMoves: ArrayList<IntArray>
	): ArrayList<IntArray> {
		val moves = ArrayList<IntArray>()
		val original = board[y][x]
		board[y][x] = 0
		for (move in potMoves) {
			val originalDeep = board[move[0]][move[1]]
			board[move[0]][move[1]] = turn * 10 + 1
			if (isPartOfNInRow(3, move[0], move[1])) {
				moves.add(move)
			}
			board[move[0]][move[1]] = originalDeep
		}
		board[y][x] = original
		return moves
	}

	fun insertMove(y: Int, x: Int) {
		if (phase == 3 || !validateMove(y, x, turn)) return
		Log.d(TAG, "Move is valid.")
		Log.d(TAG, "Player is $turn, current phase is $phase, current turntype $turnType")
		removePotentialMoves()
		handleMove(y, x)
		Log.d(TAG, "Player is $turn, current phase is $phase, current turntype $turnType")

	}

	private fun removePotentialMoves() {
		for (i in 0..4) for (j in 0..5) {
			if (board[i][j] % 10 == 3) {
				board[i][j] = 0;
			}
		}
	}

	private fun removePotentialTakeMoves() {
		val findMe = (turn % 2 + 1) * 10 + 2
		for (i in 0..4) for (j in 0..5) {
			if (board[i][j] == findMe) {
				board[i][j]--
			}
		}
	}

	private fun isPartOfNInRow(n: Int, i: Int, j: Int): Boolean {
		val type = board[i][j]
		var count = 0
		for (y in maxOf(0, i - (n - 1))..minOf(4, i + (n - 1))) {
			if (board[y][j] == type) {
				count++
				if (count == n) {
					return true
				}
			}
			else {
				count = 0
			}
		}

		count = 0
		for (x in maxOf(0, j - (n - 1))..minOf(5, j + (n - 1))) {
			if (board[i][x] == type) {
				count++
				if (count == n) {
					return true
				}
			}
			else {
				count = 0
			}
		}
		return false
	}

	private fun handleMove(y: Int, x: Int) {
		if (phase == 1) {
			board[y][x] = turn * 10 + 1
			playerPieces[turn - 1]++
			toDropCount--
			turn = turn % 2 + 1
			if (toDropCount == 0) {
				phase = 2
				turnType = 2
			}
			else {
				val potentialMoves: ArrayList<IntArray> = getPotentialDropMoves()
				// can't drop anywhere, opponent has won
				if (potentialMoves.isEmpty()) {
					phase = 3
					winner = turn % 2 + 1
				}
				else {
					addPotentialDropMoves(potentialMoves)
				}
			}
		}
		else if (phase == 2) {
			if (turnType == 2) {
				board[y][x] = turn * 10 + 2
				chosenPiece = intArrayOf(y, x)
				turnType = 3
				addPotentialPieceMoves(y, x)
			}
			else if (turnType == 3) {
				if (board[y][x] % 10 == 2) { // canceling move
					board[y][x]--
					turnType = 2
				}
				else {
					board[y][x] = turn * 10 + 1
					board[chosenPiece[0]][chosenPiece[1]] = 0
					if (isPartOfNInRow(3, y, x)) {
						val potentialTakes = getPotentialTakeMoves()
						if (potentialTakes.isEmpty()) {
							turn = turn % 2 + 1
							turnType = 2
						}
						else {
							turnType = 4
							for (take in potentialTakes) {
								board[take[0]][take[1]]++
							}
						}
					}
					else {
						turn = turn % 2 + 1
						turnType = 2
						if (getMovablePieces().isEmpty()) {
							phase = 3
							winner = turn % 2 + 1
						}
					}
				}
			}
			else if (turnType == 4) {
				board[y][x] = 0
				playerPieces[turn % 2 + 1 - 1]--
				removePotentialTakeMoves()
				if (playerPieces[turn % 2 + 1 - 1] < 3) {
					phase = 3
					winner = turn
				}
				else {
					turnType = 2
					turn = turn % 2 + 1
					if (getMovablePieces().isEmpty()) {
						phase = 3
						winner = turn % 2 + 1
					}
				}
			}
		}
	}

	private fun validateMove(y: Int, x: Int, player: Int): Boolean {
		if (player == this.turn) {
			if (this.phase == 1) return this.board[y][x] % 10 == 3  // drop phase, can drop to that spot
			else if (this.phase == 2) {     // move phase
				return when (this.turnType) {
					2 -> this.board[y][x] == this.turn * 10 + 1     // choose own piece
					3 -> this.board[y][x] == this.turn * 10 + 2 || this.board[y][x] == this.turn * 10 + 3   // place to valid location or cancel
					4 -> this.board[y][x] == (this.turn % 2 + 1) * 10 + 2    // select opponent piece to remove, has to be dark
					else -> false
				}
			}
		}
		Log.d(TAG, "turn failed")
		return false
	}

	private fun addPotentialDropMoves(potentialMoves: ArrayList<IntArray>) {
		for (move in potentialMoves) {
			board[move[0]][move[1]] = turn * 10 + 3
		}
	}

	private fun addPotentialPieceMoves(y: Int, x: Int) {
		val potentialMoves: ArrayList<IntArray> = getPotentialPieceMoves(y, x)
		for (move in potentialMoves) {
			board[move[0]][move[1]] = turn * 10 + 3
		}
	}

	private fun getPotentialDropMoves(): ArrayList<IntArray> {
		val potentialMoves: ArrayList<IntArray> = ArrayList()

		for (i in 0..4) {
			for (j in 0..5) {
				if (board[i][j] == 0) {

					board[i][j] = this.turn * 10 + 1
					if (!isPartOfNInRow(4, i, j)) {
						potentialMoves.add(intArrayOf(i, j))
					}
					board[i][j] = 0
				}
			}
		}
		return potentialMoves
	}

	private fun getAllValidDropMoves(): ArrayList<IntArray> {
		val validMoves: ArrayList<IntArray> = ArrayList()

		for (i in 0..4) for (j in 0..5) {
			if (board[i][j] % 10 == 3) {
				validMoves.add(intArrayOf(i, j))
			}
		}
		return validMoves
	}

	private fun getMovablePieces(): ArrayList<IntArray> {
		val movablePieces = ArrayList<IntArray>()
		val findMe = turn * 10 + 1
		for (i in 0..4) for (j in 0..5) {
			if (board[i][j] == findMe) {
				if (getPotentialPieceMoves(i, j).isNotEmpty()) {
					movablePieces.add(intArrayOf(i, j))
					continue
				}
			}
		}
		return movablePieces
	}

	private fun getPotentialPieceMoves(y: Int, x: Int): ArrayList<IntArray> {
		val potentialMoves: ArrayList<IntArray> = ArrayList()
		val original = board[y][x]
		board[y][x] = 0
		for (turn in TURNS) {
			val newY = y + turn[0]
			if (newY < 0 || 4 < newY) continue
			val newX = x + turn[1]
			if (newX < 0 || 5 < newX) continue
			if (board[newY][newX] == 0) {
				board[newY][newX] = this.turn * 10 + 1
				if (!isPartOfNInRow(4, newY, newX)) {
					potentialMoves.add(intArrayOf(newY, newX))
				}
				board[newY][newX] = 0
			}
		}
		board[y][x] = original
		return potentialMoves
	}

	private fun getAllValidPieceMoves(y: Int, x: Int): ArrayList<IntArray> {
		val potentialMoves: ArrayList<IntArray> = ArrayList()
		for (turn in TURNS) {
			val newY = y + turn[0]
			if (newY < 0 || 4 < newY) continue
			val newX = x + turn[1]
			if (newX < 0 || 5 < newX) continue
			if (board[newY][newX] % 10 == 3) {
				potentialMoves.add(intArrayOf(newY, newX))
			}
		}
		return potentialMoves
	}

	private fun getPotentialTakeMoves(): List<IntArray> {
		val potentialTakes = ArrayList<IntArray>()
		val findMe = (turn % 2 + 1) * 10 + 1
		for (i in 0..4) for (j in 0..5) {
			if ((board[i][j] == findMe) && !isPartOfNInRow(3, i, j)) {
				potentialTakes.add(intArrayOf(i, j))
			}
		}
		Log.d(TAG, potentialTakes.size.toString())
		return potentialTakes
	}

	private fun getAllValidTakeMoves(): List<IntArray> {
		val potentialTakes = ArrayList<IntArray>()
		val findMe = (turn % 2 + 1) * 10 + 2
		for (i in 0..4) for (j in 0..5) {
			if (board[i][j] == findMe) {
				potentialTakes.add(intArrayOf(i, j))
			}
		}
		return potentialTakes
	}


	fun getBoard(): Array<IntArray> {
		return this.board
	}

	fun getGamePhase(): Int {
		return this.phase
	}

	fun getPlayerTurn(): Int {
		return this.turn
	}

	fun getTurnType(): Int {
		return this.turnType
	}

	fun getWinner(): Int {
		return winner
	}

	fun isPlayerAi(player: Int): Boolean {
		if (player == 1) {
			return isFirstPlayerAi
		}
		else if (player == 2) {
			return isSecondPlayerAi
		}
		return false
	}

	fun getId(): Int {
		return id
	}

	fun getToDropCount(): Int {
		return toDropCount
	}

	fun getChosenPiece(): IntArray {
		return chosenPiece
	}

	fun getPlayerPieces(): IntArray {
		return playerPieces
	}

	fun isFirstPlayerAi(): Boolean {
		return isFirstPlayerAi
	}
	fun isSecondPlayerAi(): Boolean {
		return isSecondPlayerAi
	}
}