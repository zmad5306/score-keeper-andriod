package dev.zachmaddox.scorekeeper

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PlayerViewModel(context: Context) : ViewModel() {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("player_data", Context.MODE_PRIVATE)

    var players = mutableStateListOf<Player>()

    init {
        loadPlayers()
    }

    private fun loadPlayers() {
        val savedPlayers = sharedPreferences.getStringSet("players", setOf()) ?: setOf()
        players.clear()
        players.addAll(savedPlayers.map { Player(it) })
    }

    fun addPlayer(name: String) {
        players.add(Player(name))
        savePlayers()
    }

    private fun savePlayers() {
        viewModelScope.launch {
            val playerNames = players.map { it.name }.toSet()
            sharedPreferences.edit().putStringSet("players", playerNames).apply()
        }
    }
}
