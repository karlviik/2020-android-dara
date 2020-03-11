package ee.taltech.mobiilirakendused.dara

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.dara_statistics.*


class MainActivity : AppCompatActivity() {
	private lateinit var daraGame: DaraGame
	private var isGameActive = false
	private var isRedPlayerAi = false
	private var isBluePlayerAi = false
	private var triggerAllowed = false

	companion object {
		private val TAG = this::class.java.declaringClass!!.simpleName
		val buttons = arrayOf(
			arrayOf(
				R.id.squarey0x0,
				R.id.squarey0x1,
				R.id.squarey0x2,
				R.id.squarey0x3,
				R.id.squarey0x4,
				R.id.squarey0x5
			),
			arrayOf(
				R.id.squarey1x0,
				R.id.squarey1x1,
				R.id.squarey1x2,
				R.id.squarey1x3,
				R.id.squarey1x4,
				R.id.squarey1x5
			),
			arrayOf(
				R.id.squarey2x0,
				R.id.squarey2x1,
				R.id.squarey2x2,
				R.id.squarey2x3,
				R.id.squarey2x4,
				R.id.squarey2x5
			),
			arrayOf(
				R.id.squarey3x0,
				R.id.squarey3x1,
				R.id.squarey3x2,
				R.id.squarey3x3,
				R.id.squarey3x4,
				R.id.squarey3x5
			),
			arrayOf(
				R.id.squarey4x0,
				R.id.squarey4x1,
				R.id.squarey4x2,
				R.id.squarey4x3,
				R.id.squarey4x4,
				R.id.squarey4x5
			)
		)
		val gameButtonColourMap = mapOf(
			0 to R.color.colorDefault,
			11 to R.color.colorRed,
			12 to R.color.colorRedDark,
			13 to R.color.colorRedSoft,
			21 to R.color.colorBlue,
			22 to R.color.colorBlueDark,
			23 to R.color.colorBlueSoft
		)
		val PLAYER_COLOURS = arrayOf(R.color.colorRed, R.color.colorBlue)
		val FIRST_PLAYER = "RED"
		val SECOND_PLAYER = "BLUE"
		val PLAYERS = arrayOf(FIRST_PLAYER, SECOND_PLAYER)
		val WINNING_MESSAGE = " HAS WON"
		val FIRST_PHASE = "DROP PHASE"
		val SECOND_PHASE = "MOVE PHASE"
		val THIRD_PHASE = "GAME OVER"
		val PHASES = arrayOf(FIRST_PHASE, SECOND_PHASE, THIRD_PHASE)
		val FIRST_TYPE = "DROP TO LIGHT SQUARE"
		val SECOND_TYPE = "SELECT YOUR PIECE"
		val THIRD_TYPE = "MOVE TO LIGHT OR CANCEL"
		val FOURTH_TYPE = "SELECT A DARK OPPONENT PIECE"
		val TYPES = arrayOf(FIRST_TYPE, SECOND_TYPE, THIRD_TYPE, FOURTH_TYPE)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
	}

	fun gameBoardButtonOnClick(view: View) {
		Log.d(TAG, (view.id == buttons[0][0]).toString())
		if (!isGameActive || !triggerAllowed) {
			return
		}
		for (i in 0..4) for (j in 0..5) {
			if (buttons[i][j] == view.id) {
				daraGame.insertMove(i, j)
				updateGameState()
				if (daraGame.isPlayerAi(daraGame.getPlayerTurn())) {
					triggerAllowed = false
					aiMove(daraGame.getPlayerTurn())
				}
				return
			}
		}
	}

	fun startNewGame(view: View) {
		cleanBoard()
		isGameActive = true
		isRedPlayerAi = findViewById<RadioButton>(R.id.redRadioAi).isChecked
		isBluePlayerAi = findViewById<RadioButton>(R.id.blueRadioAi).isChecked
		daraGame = DaraGame(isRedPlayerAi, isBluePlayerAi)
		updateGameState()
//		if (isRedPlayerAi && isBluePlayerAi) {
//			aiVsAiGame()
//		}
//		else
		if (isRedPlayerAi) {
			triggerAllowed = false
			aiMove(1)
//			triggerAllowed = true
		}
		else {
			triggerAllowed = true
		}


	}

	// player is 1 for red, 2 for blue
	private fun aiMove(player: Int) {
		// TODO: add some delay?
		val handler = Handler()

		val gameId = daraGame.getId()

		handler.postDelayed({
			if (gameId == daraGame.getId()) {
				if (daraGame.getPlayerTurn() == player) {
					daraGame.doAiMove()
					updateGameState()
					if (daraGame.isPlayerAi(daraGame.getPlayerTurn())) {
						aiMove(daraGame.getPlayerTurn())
					}
					else {
						triggerAllowed = true
						// allow the player to start moving? have to also disable move somewhere
					}
				}
			}
		}, 500)

	}

	private fun updateGameState() {
		drawBoard(daraGame.getBoard())
		when (daraGame.getGamePhase()) {
			1 -> {
				phaseText.text = PHASES[0]
				typeText.text = TYPES[0]
			}
			2 -> {
				phaseText.text = PHASES[1]
				when (daraGame.getTurnType()) {
					2 -> typeText.text = TYPES[1]
					3 -> typeText.text = TYPES[2]
					4 -> typeText.text = TYPES[3]
				}
			}
			3 -> {
				phaseText.text = PHASES[2]
				val p = daraGame.getWinner()
				phaseText.setTextColor(resources.getColor(PLAYER_COLOURS[p - 1]))
				typeText.setTextColor(resources.getColor(PLAYER_COLOURS[p - 1]))
				typeText.text = PLAYERS[p - 1] + WINNING_MESSAGE
				return
			}
		}
		val p = daraGame.getPlayerTurn()
		phaseText.setTextColor(resources.getColor(PLAYER_COLOURS[p - 1]))
		typeText.setTextColor(resources.getColor(PLAYER_COLOURS[p - 1]))
	}

	private fun cleanBoard() {
		Log.d(TAG, "Cleaning board")
		for (array in buttons) {
			for (button in array) {
				findViewById<Button>(button).setBackgroundColor(gameButtonColourMap.getValue(0))
			}
		}
	}

	private fun drawBoard(board: Array<IntArray>) {
		for (i in 0..4) for (j in 0..5) {
			findViewById<Button>(buttons[i][j]).setBackgroundColor(
				resources.getColor(
					gameButtonColourMap.getValue(board[i][j])
				)
			)
		}
//		findViewById<Button>(buttons[0][1]).setBackgroundColor(R.color.colorRed)
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		if (::daraGame.isInitialized) {
			outState.putBoolean("daraExists", true)
			outState.putBoolean("isFirstPlayerAi", daraGame.isFirstPlayerAi())
			outState.putBoolean("isSecondPlayerAi", daraGame.isSecondPlayerAi())
			outState.putInt("phase", daraGame.getGamePhase())
			outState.putIntArray("playerPieces", daraGame.getPlayerPieces())
			outState.putIntArray("chosenPiece", daraGame.getChosenPiece())
			outState.putInt("toDropCount", daraGame.getToDropCount())
			val compactBoard = ArrayList<Int>()
			for (row in daraGame.getBoard()) {
				compactBoard.addAll(row.asIterable())
			}
			outState.putIntArray("board", compactBoard.toIntArray())
			outState.putInt("turnType", daraGame.getTurnType())
			outState.putInt("turn", daraGame.getPlayerTurn())
			outState.putInt("winner", daraGame.getWinner())
			outState.putInt("id", daraGame.getId())
		}
		else {
			Log.d(TAG, "doesn't exist")
			outState.putBoolean("daraExists", false)
		}
		outState.putBoolean("trigger", triggerAllowed)
		outState.putBoolean("isGameActive", isGameActive)
		outState.putBoolean("isRedPlayerAi", findViewById<RadioButton>(R.id.redRadioAi).isChecked)
		outState.putBoolean("isBluePlayerAi", findViewById<RadioButton>(R.id.blueRadioAi).isChecked)
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		Log.d(TAG, "lifecycle onRestoreInstanceState")
		triggerAllowed = savedInstanceState.getBoolean("trigger")
		isGameActive = savedInstanceState.getBoolean("isGameActive")
		isRedPlayerAi = savedInstanceState.getBoolean("isRedPlayerAi", findViewById<RadioButton>(R.id.redRadioAi).isChecked)
		isBluePlayerAi = savedInstanceState.getBoolean("isBluePlayerAi", findViewById<RadioButton>(R.id.blueRadioAi).isChecked)
		if (isRedPlayerAi) {
			findViewById<RadioButton>(R.id.redRadioAi).toggle()
		}
		if (isBluePlayerAi) {
			findViewById<RadioButton>(R.id.blueRadioAi).toggle()
		}

		if (savedInstanceState.getBoolean("daraExists")) {
			val expandedBoard: Array<IntArray> = Array(5) {IntArray(6)}
			val compactBoard = savedInstanceState.getIntArray("board")
			for (i in 0..4) {
				val inty = IntArray(6)
				for (j in 0..5) {
					inty[j] = compactBoard!![i * 6 + j]
				}
				expandedBoard[i] = inty
			}
			daraGame = DaraGame(
				savedInstanceState.getBoolean("isFirstPlayerAi"),
				savedInstanceState.getBoolean("isSecondPlayerAi"),
				savedInstanceState.getInt("phase"),
				savedInstanceState.getIntArray("playerPieces")!!,
				savedInstanceState.getIntArray("chosenPiece")!!,
				savedInstanceState.getInt("toDropCount"),
				expandedBoard,
				savedInstanceState.getInt("turnType"),
				savedInstanceState.getInt("turn"),
				savedInstanceState.getInt("winner"),
				savedInstanceState.getInt("id")
			)
			updateGameState()
			when (daraGame.getPlayerTurn()) {
				1 -> {
					if (isRedPlayerAi) {
						aiMove(1)
					}
				}
				2 -> {
					if (isBluePlayerAi) {
						aiMove(2)
					}
				}
			}
		}


	}

}
