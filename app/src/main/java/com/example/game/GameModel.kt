package com.example.game

enum class GameState {
    IDLE,       // "Get Ready" screen, bird bobbing up and down
    RUNNING,    // Active gameplay
    PAUSED,     // Game paused
    GAME_OVER   // Crash, scoreboard displayed
}

enum class MedalType {
    NONE,
    BRONZE,     // Score >= 10
    SILVER,     // Score >= 20
    GOLD,       // Score >= 30
    PLATINUM    // Score >= 40
}

data class PipePair(
    val id: Long,
    var x: Float,
    val gapY: Float,
    val gapHeight: Float,
    val width: Float,
    var passed: Boolean = false
) {
    val topPipeBottom: Float get() = gapY - gapHeight / 2f
    val bottomPipeTop: Float get() = gapY + gapHeight / 2f
}

data class BirdState(
    var x: Float = 0f,
    var y: Float = 0f,
    var radius: Float = 22f,
    var velocity: Float = 0f,
    var rotationAngle: Float = 0f,
    var flapFrame: Int = 0,
    var flapTimer: Float = 0f
)

data class GameStats(
    val score: Int = 0,
    val bestScore: Int = 0,
    val isNewBest: Boolean = false,
    val medal: MedalType = MedalType.NONE,
    val totalGamesPlayed: Int = 0,
    val failureMessage: String = ""
)

enum class SkyTheme {
    DAY,
    NIGHT
}
