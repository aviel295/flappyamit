package com.example.game

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import com.example.R

object GameRenderer {

    // Authentic 8-bit Flappy Bird Palette
    private val SkyDayColor = Color(0xFF4EC0CA)
    private val SkyNightColor = Color(0xFF001830)
    private val CloudColor = Color(0xFFFCFCFC)
    private val CloudShadow = Color(0xFFD0D0D0)
    private val SkylineGreen = Color(0xFF88D279)
    private val SkylineGreenDark = Color(0xFF5B9D50)
    private val SkylineNightDark = Color(0xFF142C44)
    private val SkylineWindowNight = Color(0xFFFFF078)

    // Enhanced Retro Pipe palette (Authentic multi-tone arcade shading)
    private val PipeRimHighlight = Color(0xFFD4F97E)    // Crisp specular gloss line
    private val PipeHighlightGreen = Color(0xFF9DE644)  // Vibrant lime reflection
    private val PipeBodyGreen = Color(0xFF73BF2E)       // Classic vibrant tube green
    private val PipeMidShadowGreen = Color(0xFF558A22)  // Cylindrical transition shadow
    private val PipeShadowGreen = Color(0xFF386815)     // Deep ambient shadow
    private val PipeDarkShadow = Color(0xFF22420B)      // Deepest edge shadow
    private val PipeBorderColor = Color(0xFF000000)     // Solid black arcade border
    private val PipeLipMouthColor = Color(0xFF193309)   // Dark hollow pipe interior rim
    private val PipeContactShadow = Color(0x66000000)   // 3D cast shadow under cap

    // Enhanced Retro Ground palette (Lush pixel turf, beveled dirt stripes & textured earth)
    private val GroundGrassTopHighlight = Color(0xFFA8F850) // Crisp lime grass highlight
    private val GroundGrassColor = Color(0xFF73BF2E)        // Vibrant lawn green
    private val GroundGrassShadow = Color(0xFF365E14)       // Grass blade edge shadow
    private val GroundDirtBase = Color(0xFFDDD386)          // Warm golden sand/earth
    private val GroundDirtStripe = Color(0xFFCBBF7A)        // Amber chevron stripe
    private val GroundDirtStripeHighlight = Color(0xFFEBE3A2) // Ribbon glint on stripes
    private val GroundPebbleDark = Color(0xFFA6934A)        // Dirt pebble texture
    private val GroundPebbleLight = Color(0xFFFAF2BC)       // Glistening sand grains
    private val GroundDeepEarth = Color(0xFFB5A454)         // Deep bedrock lower layer
    private val GroundBorderColor = Color(0xFF000000)       // Top black border

    // Pixel Bird palette (Authentic Flappy Bird pixel colors)
    private val BirdOutline = Color(0xFF000000)
    private val BirdYellow = Color(0xFFFAC832)
    private val BirdYellowShadow = Color(0xFFE89A14)
    private val BirdWhite = Color(0xFFFFFFFF)
    private val BirdOrange = Color(0xFFF7582B)
    private val BirdOrangeShadow = Color(0xFFC63412)
    private val BirdCheek = Color(0xFFFC8080)

    private var pixelTypeface: Typeface? = null

    fun initTypeface(context: Context) {
        if (pixelTypeface == null) {
            try {
                pixelTypeface = ResourcesCompat.getFont(context, R.font.press_start_2p)
            } catch (_: Exception) {}
        }
    }

    // Paint for Score numbers
    private val scoreFillPaint = Paint().apply {
        isAntiAlias = false
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    private val scoreStrokePaint = Paint().apply {
        isAntiAlias = false
        color = android.graphics.Color.BLACK
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.MITER
        textAlign = Paint.Align.CENTER
    }

    private val scoreShadowPaint = Paint().apply {
        isAntiAlias = false
        color = android.graphics.Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    fun renderBackground(
        drawScope: DrawScope,
        skyTheme: SkyTheme,
        groundY: Float,
        cloudOffset: Float
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height

        // 1. Sky
        val skyColor = if (skyTheme == SkyTheme.DAY) SkyDayColor else SkyNightColor
        drawScope.drawRect(
            color = skyColor,
            topLeft = Offset.Zero,
            size = Size(width, height)
        )

        // 2. Stars for night theme
        if (skyTheme == SkyTheme.NIGHT) {
            val starPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                isAntiAlias = true
            }
            val stars = listOf(
                Pair(0.12f, 0.08f), Pair(0.25f, 0.15f), Pair(0.42f, 0.05f),
                Pair(0.65f, 0.12f), Pair(0.82f, 0.06f), Pair(0.92f, 0.18f),
                Pair(0.32f, 0.22f), Pair(0.75f, 0.25f), Pair(0.18f, 0.32f),
                Pair(0.55f, 0.28f), Pair(0.88f, 0.34f)
            )
            for (s in stars) {
                drawScope.drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = 2.5f,
                    center = Offset(s.first * width, s.second * groundY)
                )
            }
        }

        // 3. Clouds (Day) or Moon (Night)
        if (skyTheme == SkyTheme.DAY) {
            val cloudY = groundY * 0.42f
            renderCloud(drawScope, (width * 0.15f + cloudOffset * 0.3f) % (width + 120f) - 60f, cloudY - 40f, 1f)
            renderCloud(drawScope, (width * 0.65f + cloudOffset * 0.3f) % (width + 120f) - 60f, cloudY - 70f, 0.85f)
            renderCloud(drawScope, (width * 0.95f + cloudOffset * 0.3f) % (width + 120f) - 60f, cloudY - 20f, 0.7f)
        } else {
            // Crescent Moon
            val moonX = width * 0.82f
            val moonY = groundY * 0.15f
            drawScope.drawCircle(
                color = Color(0xFFFFF9C4),
                radius = 28f,
                center = Offset(moonX, moonY)
            )
            drawScope.drawCircle(
                color = SkyNightColor,
                radius = 24f,
                center = Offset(moonX - 10f, moonY - 6f)
            )
        }

        // 4. Distant City Skyline
        val skylineTop = groundY - 68f
        val buildingColor = if (skyTheme == SkyTheme.DAY) SkylineGreen else SkylineNightDark
        val buildingAccent = if (skyTheme == SkyTheme.DAY) SkylineGreenDark else Color(0xFF14283E)

        // Building blocks across the width
        val bldgWidth = 44f
        var curX = 0f
        var bldgIdx = 0
        while (curX < width + bldgWidth) {
            val h = 35f + ((bldgIdx * 19) % 35)
            drawScope.drawRect(
                color = buildingColor,
                topLeft = Offset(curX, groundY - h),
                size = Size(bldgWidth - 4f, h)
            )
            // Accent stripe
            drawScope.drawRect(
                color = buildingAccent,
                topLeft = Offset(curX + bldgWidth - 10f, groundY - h),
                size = Size(6f, h)
            )

            // Lit windows at night
            if (skyTheme == SkyTheme.NIGHT && bldgIdx % 2 == 0) {
                drawScope.drawRect(
                    color = SkylineWindowNight,
                    topLeft = Offset(curX + 8f, groundY - h + 10f),
                    size = Size(6f, 8f)
                )
                drawScope.drawRect(
                    color = SkylineWindowNight,
                    topLeft = Offset(curX + 20f, groundY - h + 10f),
                    size = Size(6f, 8f)
                )
            }
            curX += bldgWidth
            bldgIdx++
        }
    }

    private fun renderCloud(drawScope: DrawScope, x: Float, y: Float, scale: Float) {
        val r = 24f * scale
        drawScope.drawCircle(color = CloudColor, radius = r, center = Offset(x, y))
        drawScope.drawCircle(color = CloudColor, radius = r * 1.3f, center = Offset(x + r * 1.1f, y - r * 0.3f))
        drawScope.drawCircle(color = CloudColor, radius = r * 1.1f, center = Offset(x + r * 2.2f, y))
        drawScope.drawRect(
            color = CloudColor,
            topLeft = Offset(x - r * 0.2f, y),
            size = Size(r * 2.5f, r * 1.1f)
        )
    }

    fun renderPipe(
        drawScope: DrawScope,
        pipe: PipePair,
        groundY: Float
    ) {
        val x = pipe.x
        val w = pipe.width
        val topBottom = pipe.topPipeBottom
        val bottomTop = pipe.bottomPipeTop
        val capHeight = 30f
        val capOverhang = 6f

        // ===== 1. TOP PIPE =====
        if (topBottom > 0) {
            // Main pipe shaft down to cap
            drawPipeColumn(
                drawScope = drawScope,
                left = x,
                top = 0f,
                width = w,
                height = topBottom - capHeight
            )
            // Top pipe end cap (facing downwards towards gap)
            drawPipeCap(
                drawScope = drawScope,
                left = x - capOverhang,
                top = topBottom - capHeight,
                width = w + capOverhang * 2f,
                height = capHeight,
                isTopPipe = true
            )
        }

        // ===== 2. BOTTOM PIPE =====
        if (bottomTop < groundY) {
            // Bottom pipe end cap (facing upwards towards gap)
            drawPipeCap(
                drawScope = drawScope,
                left = x - capOverhang,
                top = bottomTop,
                width = w + capOverhang * 2f,
                height = capHeight,
                isTopPipe = false
            )
            // Main pipe shaft down to ground with cap contact shadow
            drawPipeColumn(
                drawScope = drawScope,
                left = x,
                top = bottomTop + capHeight,
                width = w,
                height = (groundY - (bottomTop + capHeight)).coerceAtLeast(0f),
                hasTopCastShadow = true
            )
        }
    }

    private fun drawPipeColumn(
        drawScope: DrawScope,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        hasTopCastShadow: Boolean = false
    ) {
        if (height <= 0f) return

        // 1. Base vibrant tube green
        drawScope.drawRect(
            color = PipeBodyGreen,
            topLeft = Offset(left, top),
            size = Size(width, height)
        )

        // 2. Specular highlight vertical bands (tubular 3D sheen)
        drawScope.drawRect(
            color = PipeHighlightGreen,
            topLeft = Offset(left + 3.5f, top),
            size = Size(10f, height)
        )
        drawScope.drawRect(
            color = PipeRimHighlight,
            topLeft = Offset(left + 6.5f, top),
            size = Size(3.5f, height)
        )

        // 3. Right shadow bands (cylindrical curvature & ambient occlusion)
        drawScope.drawRect(
            color = PipeMidShadowGreen,
            topLeft = Offset(left + width - 26f, top),
            size = Size(10f, height)
        )
        drawScope.drawRect(
            color = PipeShadowGreen,
            topLeft = Offset(left + width - 16f, top),
            size = Size(12.5f, height)
        )
        drawScope.drawRect(
            color = PipeDarkShadow,
            topLeft = Offset(left + width - 6f, top),
            size = Size(2.5f, height)
        )

        // 4. Collar cast shadow (under cap)
        if (hasTopCastShadow && height > 6f) {
            val shadowH = 7f.coerceAtMost(height)
            drawScope.drawRect(
                color = PipeContactShadow,
                topLeft = Offset(left + 3.5f, top),
                size = Size(width - 7f, shadowH)
            )
        }

        // 5. Outer arcade black borders
        val b = 3.5f
        drawScope.drawRect(
            color = PipeBorderColor,
            topLeft = Offset(left, top),
            size = Size(b, height)
        )
        drawScope.drawRect(
            color = PipeBorderColor,
            topLeft = Offset(left + width - b, top),
            size = Size(b, height)
        )
    }

    private fun drawPipeCap(
        drawScope: DrawScope,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        isTopPipe: Boolean
    ) {
        val b = 3.5f

        // 1. Base vibrant green
        drawScope.drawRect(
            color = PipeBodyGreen,
            topLeft = Offset(left, top),
            size = Size(width, height)
        )

        // 2. Highlights aligned with shaft
        drawScope.drawRect(
            color = PipeHighlightGreen,
            topLeft = Offset(left + 9.5f, top + b),
            size = Size(11f, height - b * 2f)
        )
        drawScope.drawRect(
            color = PipeRimHighlight,
            topLeft = Offset(left + 12.5f, top + b),
            size = Size(4f, height - b * 2f)
        )

        // 3. Right shadows aligned with shaft
        drawScope.drawRect(
            color = PipeMidShadowGreen,
            topLeft = Offset(left + width - 29f, top + b),
            size = Size(11f, height - b * 2f)
        )
        drawScope.drawRect(
            color = PipeShadowGreen,
            topLeft = Offset(left + width - 18f, top + b),
            size = Size(14.5f, height - b * 2f)
        )
        drawScope.drawRect(
            color = PipeDarkShadow,
            topLeft = Offset(left + width - 7f, top + b),
            size = Size(3.5f, height - b * 2f)
        )

        // 4. Open pipe mouth / lip bevel facing the gap
        if (isTopPipe) {
            drawScope.drawRect(
                color = PipeLipMouthColor,
                topLeft = Offset(left + b, top + height - b - 2.5f),
                size = Size(width - b * 2f, 2.5f)
            )
            drawScope.drawRect(
                color = PipeDarkShadow,
                topLeft = Offset(left + b, top),
                size = Size(width - b * 2f, 2.5f)
            )
        } else {
            drawScope.drawRect(
                color = PipeLipMouthColor,
                topLeft = Offset(left + b, top + b),
                size = Size(width - b * 2f, 2.5f)
            )
            drawScope.drawRect(
                color = PipeDarkShadow,
                topLeft = Offset(left + b, top + height - b - 2.5f),
                size = Size(width - b * 2f, 2.5f)
            )
        }

        // 5. Outer arcade black border around the cap
        drawScope.drawRect(color = PipeBorderColor, topLeft = Offset(left, top), size = Size(width, b))
        drawScope.drawRect(color = PipeBorderColor, topLeft = Offset(left, top + height - b), size = Size(width, b))
        drawScope.drawRect(color = PipeBorderColor, topLeft = Offset(left, top), size = Size(b, height))
        drawScope.drawRect(color = PipeBorderColor, topLeft = Offset(left + width - b, top), size = Size(b, height))
    }

    fun renderGround(
        drawScope: DrawScope,
        groundY: Float,
        scrollOffset: Float
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height
        val groundHeight = height - groundY
        if (groundHeight <= 0) return

        // 1. Soil base layer
        drawScope.drawRect(
            color = GroundDirtBase,
            topLeft = Offset(0f, groundY),
            size = Size(width, groundHeight)
        )

        // 2. Deep bedrock layer near bottom of screen
        val bedrockH = 26f.coerceAtMost(groundHeight * 0.35f)
        if (bedrockH > 4f) {
            val by = height - bedrockH
            drawScope.drawRect(
                color = GroundDeepEarth,
                topLeft = Offset(0f, by),
                size = Size(width, bedrockH)
            )
            drawScope.drawRect(
                color = Color(0x22000000),
                topLeft = Offset(0f, by),
                size = Size(width, 2.5f)
            )
        }

        // 3. Diagonal Chevron Soil Stripes (Smooth continuous scrolling with ribbon highlights)
        val stripeWidth = 18f
        val period = stripeWidth * 2f
        val totalSpan = width + 80f
        var sx = -((scrollOffset % period) + period)
        while (sx < totalSpan) {
            val stripePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(sx, groundY + 16f)
                lineTo(sx + 14f, groundY + 16f)
                lineTo(sx - 12f, groundY + groundHeight)
                lineTo(sx - 26f, groundY + groundHeight)
                close()
            }
            drawScope.drawPath(stripePath, GroundDirtStripe)

            val highlightPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(sx + 12f, groundY + 16f)
                lineTo(sx + 14f, groundY + 16f)
                lineTo(sx - 12f, groundY + groundHeight)
                lineTo(sx - 14f, groundY + groundHeight)
                close()
            }
            drawScope.drawPath(highlightPath, GroundDirtStripeHighlight)

            sx += period
        }

        // 4. Scrolling soil pebbles & sand grains for tactile earth texture
        val pebblePeriod = 64f
        var px = -((scrollOffset % pebblePeriod) + pebblePeriod)
        while (px < width + pebblePeriod) {
            // Row 1 pebbles
            drawScope.drawRect(
                color = GroundPebbleDark,
                topLeft = Offset(px + 8f, groundY + 34f),
                size = Size(4f, 3f)
            )
            drawScope.drawRect(
                color = GroundPebbleLight,
                topLeft = Offset(px + 12f, groundY + 35f),
                size = Size(2f, 2f)
            )

            // Row 2 pebbles
            drawScope.drawRect(
                color = GroundPebbleDark,
                topLeft = Offset(px + 40f, groundY + 58f),
                size = Size(5f, 3.5f)
            )
            drawScope.drawRect(
                color = GroundPebbleLight,
                topLeft = Offset(px + 45f, groundY + 59f),
                size = Size(2.5f, 2f)
            )

            // Row 3 pebbles
            if (groundHeight > 90f) {
                drawScope.drawRect(
                    color = GroundPebbleDark,
                    topLeft = Offset(px + 22f, groundY + 80f),
                    size = Size(4f, 3f)
                )
            }
            px += pebblePeriod
        }

        // 5. Grass Layer (Lush lawn band + scrolling serrated grass fringe)
        val grassH = 16f
        drawScope.drawRect(
            color = GroundGrassColor,
            topLeft = Offset(0f, groundY),
            size = Size(width, grassH)
        )
        drawScope.drawRect(
            color = GroundGrassTopHighlight,
            topLeft = Offset(0f, groundY),
            size = Size(width, 3.5f)
        )
        drawScope.drawRect(
            color = GroundGrassShadow,
            topLeft = Offset(0f, groundY + grassH - 3f),
            size = Size(width, 3f)
        )

        // 6. Rhythmic Scrolling Grass Blades / Pixel Teeth Fringe
        val bladePeriod = 16f
        var bx = -((scrollOffset % bladePeriod) + bladePeriod)
        while (bx < width + bladePeriod) {
            val bladePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(bx, groundY + grassH)
                lineTo(bx + 6f, groundY + grassH + 5f)
                lineTo(bx + 12f, groundY + grassH)
                close()
            }
            drawScope.drawPath(bladePath, GroundGrassColor)

            val shadowBlade = androidx.compose.ui.graphics.Path().apply {
                moveTo(bx + 6f, groundY + grassH + 5f)
                lineTo(bx + 12f, groundY + grassH)
                lineTo(bx + 9f, groundY + grassH)
                close()
            }
            drawScope.drawPath(shadowBlade, GroundGrassShadow)

            bx += bladePeriod
        }

        // 7. Ground top solid black border
        drawScope.drawRect(
            color = GroundBorderColor,
            topLeft = Offset(0f, groundY),
            size = Size(width, 3.5f)
        )
    }

    fun renderBird(
        drawScope: DrawScope,
        bird: BirdState,
        painter: Painter? = null
    ) {
        val cx = bird.x
        val cy = bird.y
        val angle = bird.rotationAngle

        drawScope.rotate(degrees = angle, pivot = Offset(cx, cy)) {
            if (painter != null) {
                val intrinsic = painter.intrinsicSize
                val aspect = if (intrinsic.width > 0f && intrinsic.height > 0f && !intrinsic.width.isNaN() && !intrinsic.height.isNaN()) {
                    intrinsic.height / intrinsic.width
                } else {
                    860f / 902f
                }
                val flapFactor = when (bird.flapFrame) {
                    0 -> 1.04f
                    1 -> 1.00f
                    else -> 0.96f
                }
                val w = bird.radius * 2.85f
                val h = w * aspect * flapFactor
                translate(left = cx - w / 2f, top = cy - h / 2f) {
                    with(painter) {
                        draw(size = Size(w, h))
                    }
                }
            } else {
                BirdSprite.draw(this, cx, cy, bird.radius, bird.flapFrame)
            }
        }
    }


    fun renderScore(
        drawScope: DrawScope,
        score: Int,
        y: Float = 140f
    ) {
        val text = score.toString()
        val cx = drawScope.size.width / 2f

        val textSize = 54f
        scoreFillPaint.textSize = textSize
        scoreStrokePaint.textSize = textSize
        scoreStrokePaint.strokeWidth = 9f
        scoreShadowPaint.textSize = textSize

        if (pixelTypeface != null) {
            scoreFillPaint.typeface = pixelTypeface
            scoreStrokePaint.typeface = pixelTypeface
            scoreShadowPaint.typeface = pixelTypeface
        }

        val canvas = drawScope.drawContext.canvas.nativeCanvas
        // Pixel Drop Shadow (offset right 4, down 4)
        canvas.drawText(text, cx + 5f, y + 5f, scoreShadowPaint)
        // Pixel Outline
        canvas.drawText(text, cx, y, scoreStrokePaint)
        // White fill
        canvas.drawText(text, cx, y, scoreFillPaint)
    }
}
