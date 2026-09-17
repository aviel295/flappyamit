package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.FlappyGameViewModel
import com.example.game.GameRenderer
import com.example.game.GameState
import com.example.game.MedalType
import com.example.game.SkyTheme
import com.example.ui.theme.PressStart2P

// Authentic 8-bit Flappy Arcade Colors
private val RetroBorder = Color(0xFF000000)
private val RetroCardBg = Color(0xFFDED895)
private val RetroButtonGreen = Color(0xFF73BF2E)
private val RetroButtonOrange = Color(0xFFE65100)
private val RetroGold = Color(0xFFFAC832)

@Composable
fun FlappyGameScreen(
    viewModel: FlappyGameViewModel,
    modifier: Modifier = Modifier,
    enableGameLoop: Boolean = true,
    showInitialLoading: Boolean = enableGameLoop
) {
    val context = LocalContext.current

    val birdPainter = painterResource(id = R.drawable.ic_bird)
    LaunchedEffect(Unit) {
        GameRenderer.initTypeface(context)
    }

    var isLoading by remember { mutableStateOf(showInitialLoading) }

    val gameState by viewModel.gameState.collectAsState()
    val birdState by viewModel.birdState.collectAsState()
    val pipes by viewModel.pipes.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val skyTheme by viewModel.skyTheme.collectAsState()
    val groundScrollOffset by viewModel.groundScrollOffset.collectAsState()
    val cloudOffset by viewModel.cloudOffset.collectAsState()

    // 60FPS Game Loop Clock
    var lastFrameNanos by remember { mutableLongStateOf(0L) }
    if (enableGameLoop) {
        LaunchedEffect(Unit) {
            while (true) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameNanos != 0L) {
                        val dt = (frameTimeNanos - lastFrameNanos) / 1_000_000_000f
                        viewModel.tick(dt)
                    }
                    lastFrameNanos = frameTimeNanos
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(gameState, isLoading) {
                if (!isLoading) {
                    detectTapGestures {
                        viewModel.onTap()
                    }
                }
            }
    ) {
        // 1. GAME CANVAS
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    viewModel.onScreenMeasured(
                        coordinates.size.width.toFloat(),
                        coordinates.size.height.toFloat()
                    )
                }
        ) {
            val groundY = viewModel.groundY
            if (groundY > 0) {
                // A. Background (Sky, Sun/Moon, Clouds, Skyline)
                GameRenderer.renderBackground(
                    drawScope = this,
                    skyTheme = skyTheme,
                    groundY = groundY,
                    cloudOffset = cloudOffset
                )

                // B. Pipes
                for (pipe in pipes) {
                    GameRenderer.renderPipe(
                        drawScope = this,
                        pipe = pipe,
                        groundY = groundY
                    )
                }

                // C. Ground (Grass and moving dirt conveyor pattern)
                GameRenderer.renderGround(
                    drawScope = this,
                    groundY = groundY,
                    scrollOffset = groundScrollOffset
                )

                // D. Pixelated Classic Yellow Bird
                GameRenderer.renderBird(
                    drawScope = this,
                    bird = birdState,
                    painter = birdPainter
                )

                // E. Active Score (In-game pixel display)
                if (gameState == GameState.RUNNING || gameState == GameState.PAUSED) {
                    GameRenderer.renderScore(
                        drawScope = this,
                        score = stats.score,
                        y = 150f
                    )
                }
            }
        }

        // 2. TOP HUD CONTROLS
        TopHudBar(
            gameState = gameState,
            bestScore = stats.bestScore,
            isSoundEnabled = viewModel.audio.isSoundEnabled,
            skyTheme = skyTheme,
            onToggleSound = { viewModel.toggleSound() },
            onToggleTheme = { viewModel.toggleSkyTheme() },
            onTogglePause = { viewModel.togglePause() }
        )

        // 3. GET READY OVERLAY (IDLE STATE)
        if (gameState == GameState.IDLE) {
            GetReadyOverlay(enableAnimation = enableGameLoop)
        }

        // 4. GAME OVER SCOREBOARD (GAME_OVER STATE)
        if (gameState == GameState.GAME_OVER) {
            GameOverOverlay(
                score = stats.score,
                bestScore = stats.bestScore,
                isNewBest = stats.isNewBest,
                medal = stats.medal,
                failureMessage = stats.failureMessage,
                onRestart = { viewModel.restartGame() }
            )
        }

        // 5. PAUSED OVERLAY
        if (gameState == GameState.PAUSED) {
            PausedOverlay(
                onResume = { viewModel.togglePause() },
                onRestart = { viewModel.restartGame() }
            )
        }

        // 6. INITIAL PINK LOVE LOADING SCREEN (5 SECONDS)
        AnimatedVisibility(
            visible = isLoading,
            exit = fadeOut(animationSpec = tween(400))
        ) {
            LoveLoadingScreen(
                onLoadingFinished = { isLoading = false }
            )
        }
    }
}

@Composable
private fun TopHudBar(
    gameState: GameState,
    bestScore: Int,
    isSoundEnabled: Boolean,
    skyTheme: SkyTheme,
    onToggleSound: () -> Unit,
    onToggleTheme: () -> Unit,
    onTogglePause: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (gameState == GameState.IDLE) {
            // Pixel Best Score Box
            PixelBox(
                backgroundColor = Color(0xDD000000),
                borderColor = Color.White,
                borderWidth = 2.dp,
                modifier = Modifier.padding(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BEST $bestScore",
                        color = RetroGold,
                        fontFamily = PressStart2P,
                        fontSize = 10.sp
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Action controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day / Night Toggle
            PixelIconButton(
                onClick = onToggleTheme,
                testTag = "day_night_toggle_button"
            ) {
                Icon(
                    imageVector = if (skyTheme == SkyTheme.DAY) Icons.Default.NightlightRound else Icons.Default.WbSunny,
                    contentDescription = "Toggle Day / Night",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Sound Toggle
            PixelIconButton(
                onClick = onToggleSound,
                testTag = "sound_toggle_button"
            ) {
                Icon(
                    imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = "Toggle Sound",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Pause Button (during active run or idle)
            if (gameState == GameState.RUNNING || gameState == GameState.IDLE) {
                PixelIconButton(
                    onClick = onTogglePause,
                    testTag = "pause_button"
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GetReadyOverlay(enableAnimation: Boolean = true) {
    val handOffsetY = if (enableAnimation) {
        val infiniteTransition = rememberInfiniteTransition(label = "getReadyPulse")
        val animatedOffset by infiniteTransition.animateFloat(
            initialValue = -8f,
            targetValue = 14f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "handOffset"
        )
        animatedOffset
    } else {
        0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Authentic Pixel Title with Pixel Heart Game Logo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_pixel_heart),
                contentDescription = "Game Logo Heart",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            PixelTitle(text = "FLAPPY AMIT")
            Spacer(modifier = Modifier.width(10.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_pixel_heart),
                contentDescription = "Game Logo Heart",
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // "GET READY!" Pixel Badge
        PixelBox(
            backgroundColor = Color(0xFFFBE083),
            borderColor = RetroBorder,
            borderWidth = 3.dp
        ) {
            Text(
                text = "GET READY!",
                fontSize = 16.sp,
                fontFamily = PressStart2P,
                color = RetroButtonOrange,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Vector Bird Hero Preview
        Image(
            painter = painterResource(id = R.drawable.ic_bird),
            contentDescription = "Flappy Bird",
            modifier = Modifier.size(72.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Animated Tap Instruction Hand
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = handOffsetY.dp)
        ) {
            PixelBox(
                backgroundColor = Color(0xDD000000),
                borderColor = Color.White,
                borderWidth = 2.dp,
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(text = "👆", fontSize = 26.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            PixelBox(
                backgroundColor = Color(0xDD000000),
                borderColor = Color.White,
                borderWidth = 2.dp
            ) {
                Text(
                    text = "TAP TO FLAP",
                    color = Color.White,
                    fontFamily = PressStart2P,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun GameOverOverlay(
    score: Int,
    bestScore: Int,
    isNewBest: Boolean,
    medal: MedalType,
    failureMessage: String,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // "GAME OVER" Pixel Banner with Pink Arcade Aesthetic
        PixelBox(
            backgroundColor = Color(0xFF1F0414), // Dark magenta-black arcade box
            borderColor = Color(0xFFFF2A85),     // Vibrant hot pink border
            borderWidth = 4.dp
        ) {
            Text(
                text = "GAME OVER",
                fontSize = 20.sp,
                fontFamily = PressStart2P,
                color = Color(0xFFFF4081),       // Radiant arcade hot pink pixel text
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Vector Bird Graphic
        Image(
            painter = painterResource(id = R.drawable.ic_bird),
            contentDescription = "Flappy Bird",
            modifier = Modifier.size(60.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Encouraging Failure Message Box with Pink Pixel Aesthetic
        if (failureMessage.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .fillMaxWidth()
            ) {
                // Drop shadow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(Color(0x88000000))
                )

                // Message bubble container in cute retro pixel pink style
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF0F5)) // Soft pastel blush pink
                        .border(BorderStroke(3.dp, Color(0xFFFF4081))) // Radiant pink border
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "💖",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = failureMessage,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PressStart2P, // Pixelated font!
                        color = Color(0xFFFF1493), // Vibrant pink!
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Retro Pixel Scoreboard Box
        PixelScoreboard(
            score = score,
            bestScore = bestScore,
            isNewBest = isNewBest,
            medal = medal
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Play Again Button (Large pixel arcade button)
        PixelButton(
            text = "PLAY AGAIN",
            backgroundColor = RetroButtonGreen,
            onClick = onRestart,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .fillMaxWidth()
                .height(52.dp)
                .testTag("play_again_button")
        )
    }
}

@Composable
private fun PixelScoreboard(
    score: Int,
    bestScore: Int,
    isNewBest: Boolean,
    medal: MedalType
) {
    // Outer border with pixel step shadow
    Box(
        modifier = Modifier
            .widthIn(max = 320.dp)
            .fillMaxWidth()
    ) {
        // Black drop shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 6.dp)
                .background(Color(0x88000000))
        )

        // Main Board
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(RetroCardBg)
                .border(BorderStroke(4.dp, RetroBorder))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Medal Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MEDAL",
                        fontSize = 11.sp,
                        fontFamily = PressStart2P,
                        color = Color(0xFFFF4081)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PixelMedalBadge(medal = medal)
                }

                // Score Numbers Column
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "SCORE",
                        fontSize = 10.sp,
                        fontFamily = PressStart2P,
                        color = Color(0xFFFF4081)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = score.toString(),
                        fontSize = 20.sp,
                        fontFamily = PressStart2P,
                        color = Color(0xFF212121)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isNewBest) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFF2A85))
                                    .border(BorderStroke(1.5.dp, Color.Black))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "NEW",
                                    color = Color.White,
                                    fontFamily = PressStart2P,
                                    fontSize = 7.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = "BEST",
                            fontSize = 10.sp,
                            fontFamily = PressStart2P,
                            color = Color(0xFFFF4081)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bestScore.toString(),
                        fontSize = 18.sp,
                        fontFamily = PressStart2P,
                        color = Color(0xFF212121)
                    )
                }
            }
        }
    }
}

@Composable
private fun PixelMedalBadge(medal: MedalType) {
    val (bgColor, text, emoji) = when (medal) {
        MedalType.PLATINUM -> Triple(Color(0xFFE0F7FA), "PLAT", "💎")
        MedalType.GOLD -> Triple(Color(0xFFFFE082), "GOLD", "🥇")
        MedalType.SILVER -> Triple(Color(0xFFCFD8DC), "SLVR", "🥈")
        MedalType.BRONZE -> Triple(Color(0xFFFFCC80), "BRNZ", "🥉")
        MedalType.NONE -> Triple(Color(0xFFBDBDBD), "NONE", "⚪")
    }

    Box(
        modifier = Modifier
            .size(52.dp)
            .background(bgColor)
            .border(BorderStroke(3.dp, RetroBorder)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 24.sp)
    }
}

@Composable
private fun PausedOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .background(Color(0xFFFFF0F5)) // Soft pastel blush pink background
                .border(BorderStroke(4.dp, Color(0xFFFF2A85))) // Radiant arcade hot pink border
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "PAUSED" Pixel Banner with hearts
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "💖", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PAUSED",
                    fontSize = 18.sp,
                    fontFamily = PressStart2P,
                    color = Color(0xFFFF1493) // Hot pink
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "💖", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // User's requested pause message in pixel font
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFD1DC)) // Pastel rose background
                    .border(BorderStroke(2.5.dp, Color(0xFFFF4081)))
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "If you already paused why not send me a massage?",
                    fontSize = 11.sp,
                    fontFamily = PressStart2P,
                    color = Color(0xFF560027), // Deep royal berry pink
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            PixelButton(
                text = "RESUME",
                backgroundColor = RetroButtonGreen,
                onClick = onResume,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("resume_button")
            )

            Spacer(modifier = Modifier.height(12.dp))

            PixelButton(
                text = "RESTART",
                backgroundColor = Color(0xFFF57C00),
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("restart_from_pause_button")
            )
        }
    }
}

@Composable
private fun PixelTitle(text: String) {
    Box(contentAlignment = Alignment.Center) {
        // Drop shadow (black)
        Text(
            text = text,
            fontSize = 24.sp,
            fontFamily = PressStart2P,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(x = 4.dp, y = 4.dp)
        )
        // Foreground gold
        Text(
            text = text,
            fontSize = 24.sp,
            fontFamily = PressStart2P,
            color = RetroGold,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Pixelated button with hard-edge border and inner highlight/shadow
 */
@Composable
private fun PixelButton(
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(backgroundColor)
            .border(BorderStroke(3.dp, RetroBorder))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Top highlight line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.TopCenter)
                .background(Color(0x55FFFFFF))
        )
        // Bottom shadow line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.BottomCenter)
                .background(Color(0x55000000))
        )
        Text(
            text = text,
            fontFamily = PressStart2P,
            fontSize = 12.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Retro pixel box container with hard border
 */
@Composable
private fun PixelBox(
    backgroundColor: Color,
    borderColor: Color,
    borderWidth: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(backgroundColor)
            .border(BorderStroke(borderWidth, borderColor)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Retro square icon button
 */
@Composable
private fun PixelIconButton(
    onClick: () -> Unit,
    testTag: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .background(Color(0xDD000000))
            .border(BorderStroke(2.dp, Color.White))
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * 5-Second Pink Retro Pixel Love Loading Screen
 */
@Composable
fun LoveLoadingScreen(
    onLoadingFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
        )
        delay(150)
        onLoadingFinished()
    }

    // Gentle pulse animation for the romantic pixel bird icon
    val infiniteTransition = rememberInfiniteTransition(label = "lovePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFF85A1)) // Lovely pink background
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Romantic pulsing pixel heart logo
            Box(
                modifier = Modifier
                    .size((72 * pulseScale).dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_pixel_heart),
                    contentDescription = "Pixel Heart",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Pixelated text in a distinct shade of pink ("different kind of pink")
            // Background is #FF85A1 (Rose Candy Pink); Text is #560027 (Deep Royal Berry Pink)
            Box(contentAlignment = Alignment.Center) {
                // Pixel drop shadow in light pink for retro arcade depth
                Text(
                    text = "loading my love for you",
                    fontSize = 13.sp,
                    fontFamily = PressStart2P,
                    color = Color(0xFFFFD1DC), // Soft blush pink shadow
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier
                        .offset(x = 2.dp, y = 2.dp)
                        .padding(horizontal = 16.dp)
                )
                Text(
                    text = "loading my love for you",
                    fontSize = 13.sp,
                    fontFamily = PressStart2P,
                    color = Color(0xFF560027), // Deep berry pink
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Loading bar:
            // "outline of it will be in pink the inside in black and the loader in red"
            // "take 5 secends to load"
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .height(28.dp)
                    .border(BorderStroke(4.dp, Color(0xFFFF2A85))) // Vibrant Hot Pink outline
                    .background(Color.Black) // Inside in Black
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.value)
                        .background(Color(0xFFFF1744)) // Red loader
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Percentage in pixel font
            Text(
                text = "${(progress.value * 100).toInt()}%",
                fontSize = 11.sp,
                fontFamily = PressStart2P,
                color = Color(0xFF560027),
                textAlign = TextAlign.Center
            )
        }
    }
}

