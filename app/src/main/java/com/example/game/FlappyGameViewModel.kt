package com.example.game

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.audio.GameAudio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sin
import kotlin.random.Random

class FlappyGameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("flappy_bird_prefs", Context.MODE_PRIVATE)
    val audio = GameAudio(application)

    // Game state
    private val _gameState = MutableStateFlow(GameState.IDLE)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _birdState = MutableStateFlow(BirdState())
    val birdState: StateFlow<BirdState> = _birdState.asStateFlow()

    private val _pipes = MutableStateFlow<List<PipePair>>(emptyList())
    val pipes: StateFlow<List<PipePair>> = _pipes.asStateFlow()

    private val _stats = MutableStateFlow(GameStats())
    val stats: StateFlow<GameStats> = _stats.asStateFlow()

    private val _skyTheme = MutableStateFlow(SkyTheme.DAY)
    val skyTheme: StateFlow<SkyTheme> = _skyTheme.asStateFlow()

    private val _groundScrollOffset = MutableStateFlow(0f)
    val groundScrollOffset: StateFlow<Float> = _groundScrollOffset.asStateFlow()

    private val _cloudOffset = MutableStateFlow(0f)
    val cloudOffset: StateFlow<Float> = _cloudOffset.asStateFlow()

    // Screen dimensions
    var screenWidth = 0f
        private set
    var screenHeight = 0f
        private set
    var groundY = 0f
        private set

    // Physics constants (scaled in px)
    private var gravity = 1450f
    private var jumpVelocity = -480f
    private var maxFallVelocity = 780f
    private var pipeSpeed = 210f
    private var pipeGapHeight = 220f
    private var pipeWidth = 100f
    private var pipeDistance = 310f

    private var nextPipeId = 0L
    private var bobTimer = 0f
    private var lastPipeSpawnX = 0f

    init {
        val savedBest = prefs.getInt("best_score", 0)
        val savedGames = prefs.getInt("total_games", 0)

        _stats.value = GameStats(bestScore = savedBest, totalGamesPlayed = savedGames)
    }

    fun onScreenMeasured(width: Float, height: Float) {
        if (width <= 0 || height <= 0) return
        val isFirstMeasure = screenWidth == 0f

        screenWidth = width
        screenHeight = height
        groundY = height - (height * 0.16f).coerceAtLeast(120f)

        // Scale physics dynamically for different device resolutions
        val densityScale = (height / 800f).coerceIn(0.8f, 1.6f)
        gravity = 1450f * densityScale
        jumpVelocity = -480f * densityScale
        maxFallVelocity = 780f * densityScale
        pipeSpeed = 200f * densityScale
        pipeGapHeight = 220f * densityScale
        pipeWidth = 98f * densityScale
        pipeDistance = 300f * densityScale

        if (isFirstMeasure || _gameState.value == GameState.IDLE) {
            resetGame()
        }
    }

    fun onTap() {
        when (_gameState.value) {
            GameState.IDLE -> {
                _gameState.value = GameState.RUNNING
                flap()
            }
            GameState.RUNNING -> {
                flap()
            }
            GameState.PAUSED -> {
                _gameState.value = GameState.RUNNING
            }
            GameState.GAME_OVER -> {
                // Ignore background tap on game over; user clicks Play Again button
            }
        }
    }

    private fun flap() {
        val current = _birdState.value
        current.velocity = jumpVelocity
        current.rotationAngle = -24f
        current.flapFrame = 0
        current.flapTimer = 0f
        audio.playFlap()
    }

    fun tick(dt: Float) {
        // Clamp delta time to avoid huge leaps during lags
        val safeDt = dt.coerceIn(0.001f, 0.05f)

        when (_gameState.value) {
            GameState.IDLE -> {
                // Bobbing idle animation
                bobTimer += safeDt * 4.5f
                val startY = groundY * 0.45f
                val bobOffset = sin(bobTimer) * 14f

                val bird = _birdState.value
                bird.x = screenWidth * 0.28f
                bird.y = startY + bobOffset
                bird.rotationAngle = 0f
                bird.flapTimer += safeDt
                if (bird.flapTimer > 0.14f) {
                    bird.flapFrame = (bird.flapFrame + 1) % 3
                    bird.flapTimer = 0f
                }

                // Scroll ground and clouds
                _groundScrollOffset.value += pipeSpeed * safeDt
                _cloudOffset.value += safeDt * 12f
            }

            GameState.RUNNING -> {
                val bird = _birdState.value

                // 1. Bird Physics
                bird.velocity = (bird.velocity + gravity * safeDt).coerceAtMost(maxFallVelocity)
                bird.y += bird.velocity * safeDt

                // Rotate bird based on velocity
                if (bird.velocity < 0) {
                    bird.rotationAngle = -24f
                } else {
                    // Smoothly dive downward as velocity builds
                    val fallProgress = (bird.velocity / maxFallVelocity).coerceIn(0f, 1f)
                    bird.rotationAngle = (-24f + (78f - -24f) * fallProgress)
                }

                // Wing flapping animation
                bird.flapTimer += safeDt
                if (bird.flapTimer > 0.11f) {
                    bird.flapFrame = (bird.flapFrame + 1) % 3
                    bird.flapTimer = 0f
                }

                // 2. Scroll Ground and Clouds
                _groundScrollOffset.value += pipeSpeed * safeDt
                _cloudOffset.value += safeDt * 12f

                // 3. Update and Spawn Pipes
                val currentPipes = _pipes.value.toMutableList()

                for (pipe in currentPipes) {
                    pipe.x -= pipeSpeed * safeDt

                    // Check score passing
                    if (!pipe.passed && (pipe.x + pipe.width) < bird.x) {
                        pipe.passed = true
                        val newScore = _stats.value.score + 1
                        _stats.value = _stats.value.copy(score = newScore)
                        audio.playScore()
                    }
                }

                // Remove off-screen pipes
                currentPipes.removeAll { it.x + it.width < -40f }

                // Spawn new pipes
                val lastPipe = currentPipes.lastOrNull()
                if (lastPipe == null || (screenWidth - lastPipe.x) >= pipeDistance) {
                    val newPipe = createRandomPipe(screenWidth + 40f)
                    currentPipes.add(newPipe)
                }
                _pipes.value = currentPipes

                // 4. Collision Detection
                // A. Ground collision
                if (bird.y + bird.radius >= groundY) {
                    bird.y = groundY - bird.radius
                    triggerGameOver()
                    return
                }

                // B. Ceiling collision
                if (bird.y - bird.radius <= 0) {
                    bird.y = bird.radius
                    bird.velocity = 0f
                }

                // C. Pipe collisions
                for (pipe in currentPipes) {
                    if (checkBirdPipeCollision(bird, pipe)) {
                        triggerGameOver()
                        return
                    }
                }
            }

            GameState.GAME_OVER -> {
                // Drop bird to ground if not there
                val bird = _birdState.value
                if (bird.y + bird.radius < groundY) {
                    bird.velocity = (bird.velocity + gravity * safeDt).coerceAtMost(maxFallVelocity)
                    bird.y = (bird.y + bird.velocity * safeDt).coerceAtMost(groundY - bird.radius)
                    bird.rotationAngle = 80f
                }
            }

            GameState.PAUSED -> {
                // Frozen in place
            }
        }
    }

    private fun checkBirdPipeCollision(bird: BirdState, pipe: PipePair): Boolean {
        val bx = bird.x
        val by = bird.y
        val br = bird.radius * 0.82f // Slightly forgiving hitbox like the original game

        val pipeLeft = pipe.x
        val pipeRight = pipe.x + pipe.width
        val topBottom = pipe.topPipeBottom
        val bottomTop = pipe.bottomPipeTop

        // Horizontal overlap with pipe column
        if (bx + br >= pipeLeft && bx - br <= pipeRight) {
            // Collision with top pipe
            if (by - br <= topBottom) {
                return true
            }
            // Collision with bottom pipe
            if (by + br >= bottomTop) {
                return true
            }
        }

        return false
    }

    private fun createRandomPipe(x: Float): PipePair {
        val minMargin = 90f
        val availableHeight = groundY - pipeGapHeight - (minMargin * 2f)
        val randomOffset = if (availableHeight > 0) Random.nextFloat() * availableHeight else 0f
        val gapY = minMargin + (pipeGapHeight / 2f) + randomOffset

        return PipePair(
            id = ++nextPipeId,
            x = x,
            gapY = gapY,
            gapHeight = pipeGapHeight,
            width = pipeWidth
        )
    }

    companion object {
        val FAILURE_MESSAGES = listOf(
            "You can do better babe",
            "Good job you broke ur best score",
            "עמיתתת אני גאה בך",
            "באנה את טובה",
            "לא מאמין היית קרובה רצח",
            "יואו מה זה הוא לא קפץ",
            "I love you",
            "אני אוהב אותך",
            "לא מאמין שאת אשכרה משחקת"
        )
    }

    private fun triggerGameOver() {
        if (_gameState.value == GameState.GAME_OVER) return
        _gameState.value = GameState.GAME_OVER
        audio.playHit()
        audio.playDie()

        val score = _stats.value.score
        val prevBest = _stats.value.bestScore
        val isNewBest = score > prevBest
        val newBest = maxOf(score, prevBest)
        val totalGames = _stats.value.totalGamesPlayed + 1

        val medal = when {
            score >= 40 -> MedalType.PLATINUM
            score >= 30 -> MedalType.GOLD
            score >= 20 -> MedalType.SILVER
            score >= 10 -> MedalType.BRONZE
            else -> MedalType.NONE
        }

        // Pick a random encouraging message from the list for Amit
        val randomFailureMessage = FAILURE_MESSAGES.random()

        _stats.value = _stats.value.copy(
            bestScore = newBest,
            isNewBest = isNewBest,
            medal = medal,
            totalGamesPlayed = totalGames,
            failureMessage = randomFailureMessage
        )

        prefs.edit()
            .putInt("best_score", newBest)
            .putInt("total_games", totalGames)
            .apply()
    }

    fun restartGame() {
        audio.playButton()
        resetGame()
    }

    private fun resetGame() {
        _gameState.value = GameState.IDLE
        val bird = BirdState(
            x = screenWidth * 0.28f,
            y = groundY * 0.45f,
            radius = 24f,
            velocity = 0f,
            rotationAngle = 0f,
            flapFrame = 1
        )
        _birdState.value = bird
        _pipes.value = emptyList()
        _stats.value = _stats.value.copy(score = 0, isNewBest = false)
        bobTimer = 0f
    }

    private var stateBeforePause: GameState = GameState.RUNNING

    fun togglePause() {
        audio.playButton()
        when (_gameState.value) {
            GameState.RUNNING -> {
                stateBeforePause = GameState.RUNNING
                _gameState.value = GameState.PAUSED
            }
            GameState.IDLE -> {
                stateBeforePause = GameState.IDLE
                _gameState.value = GameState.PAUSED
            }
            GameState.PAUSED -> {
                _gameState.value = stateBeforePause
            }
            else -> {}
        }
    }

    fun toggleSkyTheme() {
        audio.playButton()
        _skyTheme.value = if (_skyTheme.value == SkyTheme.DAY) SkyTheme.NIGHT else SkyTheme.DAY
    }

    fun toggleSound() {
        audio.isSoundEnabled = !audio.isSoundEnabled
        audio.playButton()
    }

    fun toggleHaptics() {
        audio.isHapticsEnabled = !audio.isHapticsEnabled
        audio.playButton()
    }

    override fun onCleared() {
        super.onCleared()
        audio.release()
    }
}
