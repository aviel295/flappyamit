package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

data class PixelSpan(
    val x: Int,
    val y: Int,
    val width: Int,
    val color: Color
)

/**
 * Authentic high-definition Flappy Bird pixel sprite generated from the user's uploaded vector asset.
 * Features 26x11 crisp pixel spans with vibrant shading and animated wing flapping frames.
 */
object BirdSprite {
    const val WIDTH = 26
    const val HEIGHT = 11

    val bodySpans = listOf(
        PixelSpan(18, 0, 1, Color(0xFFAEA8AA)),
        PixelSpan(19, 0, 1, Color(0xFFAEA7A9)),
        PixelSpan(20, 0, 1, Color(0xFFABA6A8)),
        PixelSpan(11, 1, 1, Color(0xFFACA2A4)),
        PixelSpan(12, 1, 1, Color(0xFF806565)),
        PixelSpan(13, 1, 1, Color(0xFF4B120C)),
        PixelSpan(14, 1, 1, Color(0xFF51130D)),
        PixelSpan(15, 1, 1, Color(0xFF51140D)),
        PixelSpan(16, 1, 1, Color(0xFF4C0F09)),
        PixelSpan(17, 1, 1, Color(0xFF490E0A)),
        PixelSpan(18, 1, 1, Color(0xFF4F120E)),
        PixelSpan(19, 1, 1, Color(0xFF52140E)),
        PixelSpan(20, 1, 1, Color(0xFF51140F)),
        PixelSpan(21, 1, 1, Color(0xFF988486)),
        PixelSpan(22, 1, 1, Color(0xFFACA8AB)),
        PixelSpan(10, 2, 1, Color(0xFF806464)),
        PixelSpan(11, 2, 1, Color(0xFF521811)),
        PixelSpan(12, 2, 1, Color(0xFF7A3926)),
        PixelSpan(13, 2, 1, Color(0xFFB46E42)),
        PixelSpan(14, 2, 1, Color(0xFFB77041)),
        PixelSpan(15, 2, 1, Color(0xFFB97244)),
        PixelSpan(16, 2, 1, Color(0xFF9A5832)),
        PixelSpan(17, 2, 1, Color(0xFF925332)),
        PixelSpan(18, 2, 1, Color(0xFFA5613B)),
        PixelSpan(19, 2, 1, Color(0xFFC77D4F)),
        PixelSpan(20, 2, 1, Color(0xFFD78A5C)),
        PixelSpan(21, 2, 1, Color(0xFF773727)),
        PixelSpan(22, 2, 1, Color(0xFF643A36)),
        PixelSpan(9, 3, 1, Color(0xFF725758)),
        PixelSpan(10, 3, 1, Color(0xFF6A301F)),
        PixelSpan(11, 3, 1, Color(0xFFB86F40)),
        PixelSpan(12, 3, 1, Color(0xFFC37947)),
        PixelSpan(13, 3, 1, Color(0xFFC47A46)),
        PixelSpan(14, 3, 1, Color(0xFFB76E3D)),
        PixelSpan(15, 3, 1, Color(0xFFB66F3F)),
        PixelSpan(16, 3, 1, Color(0xFFB87241)),
        PixelSpan(17, 3, 1, Color(0xFFB36D3E)),
        PixelSpan(18, 3, 1, Color(0xFF9D5A33)),
        PixelSpan(19, 3, 1, Color(0xFF9F5D33)),
        PixelSpan(20, 3, 1, Color(0xFFBF7647)),
        PixelSpan(21, 3, 1, Color(0xFFD48653)),
        PixelSpan(22, 3, 1, Color(0xFFBC724A)),
        PixelSpan(23, 3, 1, Color(0xFF834B3D)),
        PixelSpan(24, 3, 1, Color(0xFF836C6D)),
        PixelSpan(8, 4, 1, Color(0xFF856F70)),
        PixelSpan(9, 4, 1, Color(0xFF804028)),
        PixelSpan(10, 4, 1, Color(0xFFB26A3D)),
        PixelSpan(11, 4, 1, Color(0xFFC07442)),
        PixelSpan(12, 4, 1, Color(0xFFD2854F)),
        PixelSpan(13, 4, 1, Color(0xFFC27644)),
        PixelSpan(14, 4, 1, Color(0xFFC97E4B)),
        PixelSpan(15, 4, 1, Color(0xFFBA7443)),
        PixelSpan(16, 4, 1, Color(0xFFB26D41)),
        PixelSpan(17, 4, 1, Color(0xFF975639)),
        PixelSpan(18, 4, 1, Color(0xFF5F2419)),
        PixelSpan(19, 4, 1, Color(0xFF571F15)),
        PixelSpan(20, 4, 1, Color(0xFF5E251A)),
        PixelSpan(21, 4, 1, Color(0xFFA35D3D)),
        PixelSpan(22, 4, 1, Color(0xFFD18455)),
        PixelSpan(23, 4, 1, Color(0xFFA96446)),
        PixelSpan(24, 4, 1, Color(0xFF5E3131)),
        PixelSpan(7, 5, 1, Color(0xFF988E91)),
        PixelSpan(8, 5, 1, Color(0xFF5B3131)),
        PixelSpan(9, 5, 1, Color(0xFFA05631)),
        PixelSpan(10, 5, 1, Color(0xFFBE7240)),
        PixelSpan(11, 5, 1, Color(0xFFD1824C)),
        PixelSpan(12, 5, 1, Color(0xFFBA6D3C)),
        PixelSpan(13, 5, 1, Color(0xFFD58A55)),
        PixelSpan(14, 5, 1, Color(0xFFCC8251)),
        PixelSpan(15, 5, 1, Color(0xFFB06B3F)),
        PixelSpan(16, 5, 1, Color(0xFF82442F)),
        PixelSpan(17, 5, 1, Color(0xFF7C3A32)),
        PixelSpan(18, 5, 1, Color(0xFFE3947D)),
        PixelSpan(19, 5, 1, Color(0xFFE3947A)),
        PixelSpan(20, 5, 1, Color(0xFFE1927B)),
        PixelSpan(21, 5, 1, Color(0xFF662520)),
        PixelSpan(22, 5, 1, Color(0xFFAD6642)),
        PixelSpan(23, 5, 1, Color(0xFFD48857)),
        PixelSpan(24, 5, 1, Color(0xFFAC6648)),
        PixelSpan(25, 5, 1, Color(0xFF59393A)),
        PixelSpan(7, 6, 1, Color(0xFF836F70)),
        PixelSpan(8, 6, 1, Color(0xFF713426)),
        PixelSpan(9, 6, 1, Color(0xFFB86F3D)),
        PixelSpan(10, 6, 1, Color(0xFFCD814D)),
        PixelSpan(11, 6, 1, Color(0xFFC67D48)),
        PixelSpan(12, 6, 1, Color(0xFFC27644)),
        PixelSpan(13, 6, 1, Color(0xFFD48653)),
        PixelSpan(14, 6, 1, Color(0xFFB66E41)),
        PixelSpan(15, 6, 1, Color(0xFF80412B)),
        PixelSpan(16, 6, 1, Color(0xFF7B3C34)),
        PixelSpan(17, 6, 1, Color(0xFFF7B18D)),
        PixelSpan(18, 6, 1, Color(0xFFF9B88F)),
        PixelSpan(19, 6, 1, Color(0xFFFAB88E)),
        PixelSpan(20, 6, 1, Color(0xFFFBB78E)),
        PixelSpan(21, 6, 1, Color(0xFFF9B996)),
        PixelSpan(22, 6, 1, Color(0xFF6F2D23)),
        PixelSpan(23, 6, 1, Color(0xFFB97146)),
        PixelSpan(24, 6, 1, Color(0xFFB46A45)),
        PixelSpan(25, 6, 1, Color(0xFF633A3B)),
        PixelSpan(6, 7, 1, Color(0xFF988587)),
        PixelSpan(7, 7, 1, Color(0xFF866865)),
        PixelSpan(8, 7, 1, Color(0xFF7E3F27)),
        PixelSpan(9, 7, 1, Color(0xFFC37946)),
        PixelSpan(10, 7, 1, Color(0xFFC17844)),
        PixelSpan(11, 7, 1, Color(0xFFBE7542)),
        PixelSpan(12, 7, 1, Color(0xFFCB804D)),
        PixelSpan(13, 7, 1, Color(0xFFC3794A)),
        PixelSpan(14, 7, 1, Color(0xFF82422C)),
        PixelSpan(15, 7, 1, Color(0xFFAA6A5B)),
        PixelSpan(16, 7, 1, Color(0xFFF2AE8F)),
        PixelSpan(17, 7, 1, Color(0xFFF7B78C)),
        PixelSpan(18, 7, 1, Color(0xFFF8B88E)),
        PixelSpan(19, 7, 2, Color(0xFFF9B88E)),
        PixelSpan(21, 7, 1, Color(0xFFF8B88E)),
        PixelSpan(22, 7, 1, Color(0xFF662520)),
        PixelSpan(23, 7, 1, Color(0xFFD48653)),
        PixelSpan(24, 7, 1, Color(0xFFB46A45)),
        PixelSpan(25, 7, 1, Color(0xFF633A3B)),
        PixelSpan(7, 8, 1, Color(0xFF866865)),
        PixelSpan(8, 8, 1, Color(0xFF713426)),
        PixelSpan(9, 8, 1, Color(0xFFB86F3D)),
        PixelSpan(10, 8, 1, Color(0xFFC07442)),
        PixelSpan(11, 8, 1, Color(0xFFC47A46)),
        PixelSpan(12, 8, 1, Color(0xFFB66E41)),
        PixelSpan(13, 8, 1, Color(0xFF9D5A33)),
        PixelSpan(14, 8, 1, Color(0xFF80412B)),
        PixelSpan(15, 8, 1, Color(0xFF713426)),
        PixelSpan(16, 8, 1, Color(0xFF6A301F)),
        PixelSpan(17, 8, 1, Color(0xFFD48653)),
        PixelSpan(18, 8, 1, Color(0xFFBF7647)),
        PixelSpan(19, 8, 1, Color(0xFF9F5D33)),
        PixelSpan(20, 8, 1, Color(0xFF834B3D)),
        PixelSpan(21, 8, 1, Color(0xFF662520)),
        PixelSpan(8, 9, 1, Color(0xFF6A301F)),
        PixelSpan(9, 9, 1, Color(0xFF804028)),
        PixelSpan(10, 9, 1, Color(0xFF9A5832)),
        PixelSpan(11, 9, 1, Color(0xFF804028)),
        PixelSpan(12, 9, 1, Color(0xFF6A301F)),
        PixelSpan(13, 9, 1, Color(0xFF521811)),
        PixelSpan(17, 9, 1, Color(0xFF521811)),
        PixelSpan(18, 9, 1, Color(0xFF662520)),
        PixelSpan(19, 9, 1, Color(0xFF521811)),
        PixelSpan(9, 10, 1, Color(0xFF521811)),
        PixelSpan(10, 10, 1, Color(0xFF4B120C)),
        PixelSpan(11, 10, 1, Color(0xFF51130D)),
        PixelSpan(12, 10, 1, Color(0xFF521811)),
    )

    val wingSpans = listOf(
        PixelSpan(1, 3, 1, Color(0xFF8A7375)),
        PixelSpan(2, 3, 1, Color(0xFF6E4E4F)),
        PixelSpan(0, 4, 1, Color(0xFF73595D)),
        PixelSpan(1, 4, 1, Color(0xFF6C301C)),
        PixelSpan(2, 4, 1, Color(0xFFA76B48)),
        PixelSpan(3, 4, 1, Color(0xFF9A8486)),
        PixelSpan(0, 5, 1, Color(0xFF74585B)),
        PixelSpan(1, 5, 1, Color(0xFF935027)),
        PixelSpan(2, 5, 1, Color(0xFFE7A956)),
        PixelSpan(3, 5, 1, Color(0xFF8F644E)),
        PixelSpan(4, 5, 1, Color(0xFF95878A)),
        PixelSpan(0, 6, 1, Color(0xFF76595C)),
        PixelSpan(1, 6, 1, Color(0xFF914F27)),
        PixelSpan(2, 6, 1, Color(0xFFCD8736)),
        PixelSpan(3, 6, 1, Color(0xFFF2B861)),
        PixelSpan(4, 6, 1, Color(0xFF6E4437)),
        PixelSpan(0, 7, 1, Color(0xFF938587)),
        PixelSpan(1, 7, 1, Color(0xFF824E39)),
        PixelSpan(2, 7, 1, Color(0xFFC8843A)),
        PixelSpan(3, 7, 1, Color(0xFFC48132)),
        PixelSpan(4, 7, 1, Color(0xFFDA9E54)),
        PixelSpan(5, 7, 1, Color(0xFF866051)),
        PixelSpan(1, 8, 1, Color(0xFF824E39)),
        PixelSpan(2, 8, 1, Color(0xFFC8843A)),
        PixelSpan(3, 8, 1, Color(0xFFCD8736)),
        PixelSpan(4, 8, 1, Color(0xFF824E39)),
        PixelSpan(5, 8, 1, Color(0xFF866865)),
        PixelSpan(2, 9, 1, Color(0xFF6E4E4F)),
        PixelSpan(3, 9, 1, Color(0xFF824E39)),
        PixelSpan(4, 9, 1, Color(0xFF6E4E4F)),
    )

    fun draw(
        drawScope: DrawScope,
        cx: Float,
        cy: Float,
        radius: Float,
        flapFrame: Int
    ) {
        val pixelSize = (radius * 2.2f) / 14f
        val originX = cx - (13f * pixelSize)
        val originY = cy - (5.5f * pixelSize)

        // 1. Draw main body, head, eye, and beak
        for (span in bodySpans) {
            drawScope.drawRect(
                color = span.color,
                topLeft = Offset(originX + span.x * pixelSize, originY + span.y * pixelSize),
                size = Size(span.width * pixelSize, pixelSize)
            )
        }

        // 2. Wing flapping vertical offset (0: Wing Up, 1: Wing Mid, 2: Wing Down)
        val wingDy = when (flapFrame) {
            0 -> -1f
            1 -> 0f
            else -> 1f
        }

        for (span in wingSpans) {
            drawScope.drawRect(
                color = span.color,
                topLeft = Offset(originX + span.x * pixelSize, originY + (span.y + wingDy) * pixelSize),
                size = Size(span.width * pixelSize, pixelSize)
            )
        }
    }
}
