package dev.zachmaddox.scorekeeper

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch

class PlayerViewModel(context: Context) : ViewModel() {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("player_data", Context.MODE_PRIVATE)

    var players = mutableStateListOf<Player>()

    init {
        loadPlayers()
    }

    private fun loadPlayers() {
        val savedPlayersJsons = sharedPreferences.getStringSet("players", setOf()) ?: setOf()
        players.clear()
        players.addAll(savedPlayersJsons.map { Gson().fromJson(it, Player::class.java) })
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

    private fun savePlayers() {
        viewModelScope.launch {
            val playersAsJson = players.map { Gson().toJson(it) }.toSet()
            sharedPreferences.edit().putStringSet("players", playersAsJson).apply()
        }
    }
}
