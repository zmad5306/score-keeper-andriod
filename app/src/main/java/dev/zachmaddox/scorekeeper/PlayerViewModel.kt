package dev.zachmaddox.scorekeeper

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch

class PlayerViewModel(context: Context) : ViewModel() {

    private val playerSharedPreferences: SharedPreferences = context.getSharedPreferences("player_data", Context.MODE_PRIVATE)
    private val gameSharedPreferences: SharedPreferences = context.getSharedPreferences("game_data", Context.MODE_PRIVATE)

    var players = mutableStateListOf<Player>()
    val playingTo = mutableIntStateOf(0)

    init {
        loadPlayers()
        loadGame()
    }

    private fun loadPlayers() {
        val savedPlayersJsons = playerSharedPreferences.getStringSet("players", setOf()) ?: setOf()
        players.clear()
        players.addAll(savedPlayersJsons.map { Gson().fromJson(it, Player::class.java) })
    }

    private fun loadGame() {
        playingTo.intValue = gameSharedPreferences.getInt("playingTo", 0)
    }

    fun addPlayer(name: String) {
        players.add(Player(name))
        savePlayers()
    }

    fun incrementScore(player: Player, points: Int) {
        player.score += points
        savePlayers()
    }

    fun removePlayer(player: Player) {
        players.remove(player)
    }

    fun resetScores() {
        players.forEach({it.score = 0})
        savePlayers()
    }

    fun setPlayingTo(pt: Int) {
        playingTo.intValue = pt
        saveGame()
    }

    private fun savePlayers() {
        viewModelScope.launch {
            val playersAsJson = players.map { Gson().toJson(it) }.toSet()
            playerSharedPreferences.edit().putStringSet("players", playersAsJson).apply()
        }
    }

    private fun saveGame() {
        viewModelScope.launch {
            gameSharedPreferences.edit().putInt("playingTo", playingTo.intValue).apply()
        }
    }

}
