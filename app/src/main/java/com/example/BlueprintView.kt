package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BlueprintDrawing(
    state: ElevatorConfigState,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    val larguraPoco = state.larguraPoco.toFloatOrNull() ?: 1700f
    val profundidadePoco = state.profundidadePoco.toFloatOrNull() ?: 1545f
    val larguraCabine = state.larguraCabine.toFloatOrNull() ?: 800f
    val profundidadeCabine = state.profundidadeCabine.toFloatOrNull() ?: 1250f
    val aberturaPorta = state.aberturaPorta.toFloatOrNull() ?: 800f
    val espacoChassis = state.espacoChassis.toFloatOrNull() ?: 450f
    val menorFolga = state.menorFolga.toFloatOrNull() ?: 125f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF081426)) // Deep blueprint background
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // 1. Blueprint Technical Grid
        val gridSize = 24.dp.toPx()
        val gridColor = Color(0x12FFFFFF)
        val gridThickColor = Color(0x2864FFDA)
        
        for (i in 0..(canvasWidth / gridSize).toInt()) {
            val isMajor = i % 4 == 0
            drawLine(
                color = if (isMajor) gridThickColor else gridColor,
                start = Offset(i * gridSize, 0f),
                end = Offset(i * gridSize, canvasHeight),
                strokeWidth = if (isMajor) 1.5f else 0.8f
            )
        }
        for (i in 0..(canvasHeight / gridSize).toInt()) {
            val isMajor = i % 4 == 0
            drawLine(
                color = if (isMajor) gridThickColor else gridColor,
                start = Offset(0f, i * gridSize),
                end = Offset(canvasWidth, i * gridSize),
                strokeWidth = if (isMajor) 1.5f else 0.8f
            )
        }

        if (larguraPoco <= 0f || profundidadePoco <= 0f) return@Canvas

        // 2. Proportional Scaling & Margin calculations
        val leftPadding = 56.dp.toPx()
        val rightPadding = 56.dp.toPx()
        val topPadding = 50.dp.toPx()
        val bottomPadding = 75.dp.toPx()

        val availableWidth = canvasWidth - (leftPadding + rightPadding)
        val availableHeight = canvasHeight - (topPadding + bottomPadding)

        val scale = minOf(availableWidth / larguraPoco, availableHeight / profundidadePoco)

        val pocoDrawWidth = larguraPoco * scale
        val pocoDrawHeight = profundidadePoco * scale
        val cabineDrawWidth = larguraCabine * scale
        val cabineDrawHeight = profundidadeCabine * scale
        val doorDrawWidth = minOf(aberturaPorta * scale, cabineDrawWidth * 0.95f)
        val espChassisScaled = espacoChassis * scale
        val menorFolgaScaled = menorFolga * scale

        val pocoLeft = leftPadding + (availableWidth - pocoDrawWidth) / 2f
        val pocoTop = topPadding + (availableHeight - pocoDrawHeight) / 2f
        val wallThickness = 7.dp.toPx()

        // 3. Cabin Position inside Shaft based on Chassis Side & Entrances
        val isChassisSide = state.ladoArcada == "Lateral"
        val isChassisBack = state.ladoArcada == "Fundo"
        val isElectric = state.acionamento == "Elétrico"
        val isHydraulic = state.acionamento == "Hidráulico"
        val isSuspension = state.arcada == "Suspensão"
        val isEntranceOpposite = state.tipoEntrada == "Oposta"
        val isEntranceAdjacent = state.tipoEntrada == "Adjacente"

        val cbX: Float
        val cbY: Float

        if (isChassisSide) {
            // Chassis on the left or right? Let's place chassis on the left side
            val leftClearance = espChassisScaled
            val rightClearance = if (isEntranceAdjacent) 160f * scale else menorFolgaScaled
            val availableShaftInteriorX = pocoDrawWidth - cabineDrawWidth
            cbX = pocoLeft + leftClearance.coerceAtMost(availableShaftInteriorX * 0.7f)
            
            val frontDoorClearance = 160f * scale
            val rearClearance = if (isEntranceOpposite) 160f * scale else 120f * scale
            val availableShaftInteriorY = pocoDrawHeight - cabineDrawHeight
            cbY = pocoTop + rearClearance.coerceAtMost(availableShaftInteriorY * 0.5f)
        } else {
            // Chassis is at the rear (fundo)
            val rearClearance = espChassisScaled
            val frontDoorClearance = 160f * scale
            val availableShaftInteriorY = pocoDrawHeight - cabineDrawHeight
            cbY = pocoTop + rearClearance.coerceAtMost(availableShaftInteriorY * 0.7f)
            
            val leftClearance = menorFolgaScaled
            val rightClearance = if (isEntranceAdjacent) 160f * scale else menorFolgaScaled
            cbX = pocoLeft + (pocoDrawWidth - cabineDrawWidth) / 2f
        }

        val cbRight = cbX + cabineDrawWidth
        val cbBottom = cbY + cabineDrawHeight
        val cbCenterX = cbX + cabineDrawWidth / 2f
        val cbCenterY = cbY + cabineDrawHeight / 2f

        // 4. Concrete Shaft Walls with Door Openings
        drawRect(
            color = Color(0x2864FFDA),
            topLeft = Offset(pocoLeft - wallThickness, pocoTop - wallThickness),
            size = Size(pocoDrawWidth + wallThickness * 2, pocoDrawHeight + wallThickness * 2)
        )
        drawRect(
            color = Color(0xFF081426),
            topLeft = Offset(pocoLeft, pocoTop),
            size = Size(pocoDrawWidth, pocoDrawHeight)
        )

        // Hatching lines on concrete walls
        val hatchStep = 10.dp.toPx()
        val hatchColor = Color(0x2E64FFDA)
        for (offset in -pocoDrawHeight.toInt()..(pocoDrawWidth + wallThickness * 2).toInt() step hatchStep.toInt()) {
            val oX = pocoLeft - wallThickness + offset
            drawLine(
                color = hatchColor,
                start = Offset(oX, pocoTop - wallThickness),
                end = Offset(oX + wallThickness * 2, pocoTop + wallThickness),
                strokeWidth = 1f
            )
            drawLine(
                color = hatchColor,
                start = Offset(oX, pocoTop + pocoDrawHeight),
                end = Offset(oX + wallThickness * 2, pocoTop + pocoDrawHeight + wallThickness),
                strokeWidth = 1f
            )
        }

        // Shaft outlines
        drawRect(
            color = Color(0xFF90CDF4),
            topLeft = Offset(pocoLeft - wallThickness, pocoTop - wallThickness),
            size = Size(pocoDrawWidth + wallThickness * 2, pocoDrawHeight + wallThickness * 2),
            style = Stroke(width = 1.5f)
        )
        drawRect(
            color = Color(0xFFE2E8F0),
            topLeft = Offset(pocoLeft, pocoTop),
            size = Size(pocoDrawWidth, pocoDrawHeight),
            style = Stroke(width = 2.5f)
        )

        // 5. Door Openings in Shaft Walls (Cutouts)
        val frontDoorLeft = cbX + (cabineDrawWidth - doorDrawWidth) / 2f
        
        // Front Door Cutout (Bottom)
        drawRect(
            color = Color(0xFF081426),
            topLeft = Offset(frontDoorLeft, pocoTop + pocoDrawHeight - 1.5f),
            size = Size(doorDrawWidth, wallThickness + 4f)
        )

        // Rear Door Cutout (Top) if Entrada Oposta
        if (isEntranceOpposite) {
            val rearDoorLeft = cbX + (cabineDrawWidth - doorDrawWidth) / 2f
            drawRect(
                color = Color(0xFF081426),
                topLeft = Offset(rearDoorLeft, pocoTop - wallThickness - 2f),
                size = Size(doorDrawWidth, wallThickness + 4f)
            )
        }

        // Side Door Cutout (Right) if Entrada Adjacente
        val sideDoorTop = cbY + (cabineDrawHeight - doorDrawWidth) / 2f
        if (isEntranceAdjacent) {
            drawRect(
                color = Color(0xFF081426),
                topLeft = Offset(pocoLeft + pocoDrawWidth - 1.5f, sideDoorTop),
                size = Size(wallThickness + 4f, doorDrawWidth)
            )
        }

        // 6. Drive Element: Counterweight (C.P.) or Hydraulic Piston
        val accentOrange = Color(0xFFFFAB40)
        val hydraulicBlue = Color(0xFF00E5FF)
        val railColor = Color(0xFFECEFF1)

        var driveCenterX = 0f
        var driveCenterY = 0f

        if (isChassisSide) {
            val spaceX = cbX - pocoLeft
            driveCenterX = pocoLeft + spaceX / 2f
            driveCenterY = if (state.posicao == "Deslocado") {
                pocoTop + pocoDrawHeight * 0.28f // shifted towards rear corner
            } else {
                cbCenterY // centered with cabin
            }

            if (isElectric) {
                // Counterweight on side
                val cpWidth = (spaceX * 0.42f).coerceIn(16.dp.toPx(), 28.dp.toPx())
                val cpHeight = (cabineDrawHeight * 0.65f).coerceIn(40.dp.toPx(), 120.dp.toPx())
                val cpLeft = driveCenterX - cpWidth / 2f
                val cpTop = driveCenterY - cpHeight / 2f

                drawCounterweight(cpLeft, cpTop, cpWidth, cpHeight, accentOrange)
                // Counterweight guide rails
                drawGuideRail(driveCenterX, cpTop, angle = 0f, railColor)
                drawGuideRail(driveCenterX, cpTop + cpHeight, angle = 180f, railColor)
            } else {
                // Hydraulic Cylinder & Piston
                val pistonRadius = (spaceX * 0.28f).coerceIn(10.dp.toPx(), 20.dp.toPx())
                drawHydraulicPiston(driveCenterX, driveCenterY, pistonRadius, hydraulicBlue)
                // Hydraulic bracket / guide
                drawGuideRail(driveCenterX, driveCenterY - pistonRadius - 4.dp.toPx(), angle = 0f, railColor)
                drawGuideRail(driveCenterX, driveCenterY + pistonRadius + 4.dp.toPx(), angle = 180f, railColor)
            }
        } else {
            // Chassis on the rear wall (Fundo)
            val spaceY = cbY - pocoTop
            driveCenterY = pocoTop + spaceY / 2f
            driveCenterX = if (state.posicao == "Deslocado") {
                pocoLeft + pocoDrawWidth * 0.25f // shifted to corner
            } else {
                cbCenterX // centered with cabin
            }

            if (isElectric) {
                // Counterweight on rear
                val cpWidth = (cabineDrawWidth * 0.65f).coerceIn(40.dp.toPx(), 140.dp.toPx())
                val cpHeight = (spaceY * 0.45f).coerceIn(16.dp.toPx(), 28.dp.toPx())
                val cpLeft = driveCenterX - cpWidth / 2f
                val cpTop = driveCenterY - cpHeight / 2f

                drawCounterweight(cpLeft, cpTop, cpWidth, cpHeight, accentOrange)
                // Counterweight guide rails
                drawGuideRail(cpLeft, driveCenterY, angle = -90f, railColor)
                drawGuideRail(cpLeft + cpWidth, driveCenterY, angle = 90f, railColor)
            } else {
                // Hydraulic Cylinder on rear
                val pistonRadius = (spaceY * 0.32f).coerceIn(10.dp.toPx(), 20.dp.toPx())
                drawHydraulicPiston(driveCenterX, driveCenterY, pistonRadius, hydraulicBlue)
                drawGuideRail(driveCenterX - pistonRadius - 4.dp.toPx(), driveCenterY, angle = -90f, railColor)
                drawGuideRail(driveCenterX + pistonRadius + 4.dp.toPx(), driveCenterY, angle = 90f, railColor)
            }
        }

        // 7. Arcada / Sling Frame Structure (L vs Suspensão)
        val slingColor = Color(0xCC64FFDA)
        val slingStroke = 2.5f

        if (isSuspension) {
            // Dual-side uprights and cross sling passing through cabin center
            drawLine(
                color = slingColor,
                start = Offset(cbX - 4.dp.toPx(), cbCenterY),
                end = Offset(cbRight + 4.dp.toPx(), cbCenterY),
                strokeWidth = slingStroke
            )
            // Cabin guide rails on left and right center
            drawGuideRail(cbX, cbCenterY, angle = -90f, railColor)
            drawGuideRail(cbRight, cbCenterY, angle = 90f, railColor)
        } else {
            // Arcada "L" (Cantilever / Mochila)
            // Structural bracket arm from the chassis side supporting the cabin
            if (isChassisSide) {
                // Arm extends from the chassis side under/along the cabin
                drawRect(
                    color = slingColor,
                    topLeft = Offset(cbX - 4.dp.toPx(), cbCenterY - 14.dp.toPx()),
                    size = Size(8.dp.toPx(), 28.dp.toPx()),
                    style = Stroke(width = 2f)
                )
                drawLine(
                    color = slingColor,
                    start = Offset(cbX, cbCenterY - 14.dp.toPx()),
                    end = Offset(cbX + cabineDrawWidth * 0.6f, cbCenterY - 14.dp.toPx()),
                    strokeWidth = 2f
                )
                drawLine(
                    color = slingColor,
                    start = Offset(cbX, cbCenterY + 14.dp.toPx()),
                    end = Offset(cbX + cabineDrawWidth * 0.6f, cbCenterY + 14.dp.toPx()),
                    strokeWidth = 2f
                )
                // Main dual rails on the chassis side for L frame
                drawGuideRail(cbX, cbCenterY - 14.dp.toPx(), angle = -90f, railColor)
                drawGuideRail(cbX, cbCenterY + 14.dp.toPx(), angle = -90f, railColor)
            } else {
                // L Arcada at the rear
                drawRect(
                    color = slingColor,
                    topLeft = Offset(cbCenterX - 14.dp.toPx(), cbY - 4.dp.toPx()),
                    size = Size(28.dp.toPx(), 8.dp.toPx()),
                    style = Stroke(width = 2f)
                )
                drawLine(
                    color = slingColor,
                    start = Offset(cbCenterX - 14.dp.toPx(), cbY),
                    end = Offset(cbCenterX - 14.dp.toPx(), cbY + cabineDrawHeight * 0.6f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = slingColor,
                    start = Offset(cbCenterX + 14.dp.toPx(), cbY),
                    end = Offset(cbCenterX + 14.dp.toPx(), cbY + cabineDrawHeight * 0.6f),
                    strokeWidth = 2f
                )
                drawGuideRail(cbCenterX - 14.dp.toPx(), cbY, angle = 0f, railColor)
                drawGuideRail(cbCenterX + 14.dp.toPx(), cbY, angle = 0f, railColor)
            }
        }

        // 8. Draw Cabin Body (Cabine)
        val cabinBorderColor = Color(0xFFFFFFFF)
        val cabinInnerColor = Color(0xFF64FFDA)

        // Outer cabin wall
        drawRect(
            color = cabinBorderColor,
            topLeft = Offset(cbX, cbY),
            size = Size(cabineDrawWidth, cabineDrawHeight),
            style = Stroke(width = 2.5f)
        )
        // Inner finish line
        drawRect(
            color = cabinInnerColor.copy(alpha = 0.5f),
            topLeft = Offset(cbX + 2.5.dp.toPx(), cbY + 2.5.dp.toPx()),
            size = Size(cabineDrawWidth - 5.dp.toPx(), cabineDrawHeight - 5.dp.toPx()),
            style = Stroke(width = 1f)
        )

        // Cabin Center Crosshair
        val chSize = 8.dp.toPx()
        drawLine(color = Color(0x44FFFFFF), start = Offset(cbCenterX - chSize, cbCenterY), end = Offset(cbCenterX + chSize, cbCenterY), strokeWidth = 1f)
        drawLine(color = Color(0x44FFFFFF), start = Offset(cbCenterX, cbCenterY - chSize), end = Offset(cbCenterX, cbCenterY + chSize), strokeWidth = 1f)

        // 9. Draw Doors according to tipoPorta and tipoEntrada
        // Front Entrance (Bottom)
        drawSlidingDoors(
            doorType = state.tipoPorta,
            doorLeft = frontDoorLeft,
            doorWidth = doorDrawWidth,
            cabineY = cbBottom,
            landingY = pocoTop + pocoDrawHeight + wallThickness / 2f,
            isVertical = false,
            primaryColor = Color(0xFF64FFDA),
            secondaryColor = Color(0xFFB0BEC5)
        )

        // Opposite Entrance (Top)
        if (isEntranceOpposite) {
            val rearDoorLeft = cbX + (cabineDrawWidth - doorDrawWidth) / 2f
            drawSlidingDoors(
                doorType = state.tipoPorta,
                doorLeft = rearDoorLeft,
                doorWidth = doorDrawWidth,
                cabineY = cbY,
                landingY = pocoTop - wallThickness / 2f,
                isVertical = false,
                primaryColor = Color(0xFF64FFDA),
                secondaryColor = Color(0xFFB0BEC5)
            )
        }

        // Adjacent Entrance (Right)
        if (isEntranceAdjacent) {
            drawSlidingDoors(
                doorType = state.tipoPorta,
                doorLeft = sideDoorTop,
                doorWidth = doorDrawWidth,
                cabineY = cbRight,
                landingY = pocoLeft + pocoDrawWidth + wallThickness / 2f,
                isVertical = true,
                primaryColor = Color(0xFF64FFDA),
                secondaryColor = Color(0xFFB0BEC5)
            )
        }

        // 10. Technical Cotas & Dimensions
        val textStyle = TextStyle(color = Color.White, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
        val highlightTextStyle = TextStyle(color = Color(0xFF64FFDA), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        val orangeTextStyle = TextStyle(color = accentOrange, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)

        // Dimension helper
        fun drawDim(x1: Float, y1: Float, x2: Float, y2: Float, label: String, isHoriz: Boolean, offsetPx: Float, style: TextStyle = textStyle) {
            val tick = 3.5.dp.toPx()
            if (isHoriz) {
                val y = y1 + offsetPx
                drawLine(color = Color(0x88FFFFFF), start = Offset(x1, y), end = Offset(x2, y), strokeWidth = 1f)
                drawLine(color = Color.White, start = Offset(x1 - tick, y + tick), end = Offset(x1 + tick, y - tick), strokeWidth = 1.5f)
                drawLine(color = Color.White, start = Offset(x2 - tick, y + tick), end = Offset(x2 + tick, y - tick), strokeWidth = 1.5f)
                
                // Extension witness lines
                drawLine(color = Color(0x33FFFFFF), start = Offset(x1, y1), end = Offset(x1, y), strokeWidth = 0.8f)
                drawLine(color = Color(0x33FFFFFF), start = Offset(x2, y2), end = Offset(x2, y), strokeWidth = 0.8f)

                val m = textMeasurer.measure(label, style)
                drawText(m, topLeft = Offset((x1 + x2)/2f - m.size.width/2f, y - m.size.height - 1.dp.toPx()))
            } else {
                val x = x1 + offsetPx
                drawLine(color = Color(0x88FFFFFF), start = Offset(x, y1), end = Offset(x, y2), strokeWidth = 1f)
                drawLine(color = Color.White, start = Offset(x - tick, y1 + tick), end = Offset(x + tick, y1 - tick), strokeWidth = 1.5f)
                drawLine(color = Color.White, start = Offset(x - tick, y2 + tick), end = Offset(x + tick, y2 - tick), strokeWidth = 1.5f)

                // Extension witness lines
                drawLine(color = Color(0x33FFFFFF), start = Offset(x1, y1), end = Offset(x, y1), strokeWidth = 0.8f)
                drawLine(color = Color(0x33FFFFFF), start = Offset(x2, y2), end = Offset(x, y2), strokeWidth = 0.8f)

                val m = textMeasurer.measure(label, style)
                drawText(m, topLeft = Offset(x - m.size.width - 3.dp.toPx(), (y1 + y2)/2f - m.size.height/2f))
            }
        }

        // Shaft Dimensions (Poço)
        drawDim(pocoLeft, pocoTop, pocoLeft + pocoDrawWidth, pocoTop, "LP: ${state.larguraPoco} mm", isHoriz = true, offsetPx = -16.dp.toPx(), highlightTextStyle)
        drawDim(pocoLeft, pocoTop, pocoLeft, pocoTop + pocoDrawHeight, "PP: ${state.profundidadePoco}", isHoriz = false, offsetPx = -16.dp.toPx(), highlightTextStyle)

        // Cabin Dimensions (Cabine)
        drawDim(cbX, cbBottom, cbRight, cbBottom, "LC: ${state.larguraCabine} mm", isHoriz = true, offsetPx = 18.dp.toPx(), textStyle)
        drawDim(cbRight, cbY, cbRight, cbBottom, "${state.profundidadeCabine}", isHoriz = false, offsetPx = 16.dp.toPx(), textStyle)

        // Door Opening Dimension (Abertura Vão)
        drawDim(frontDoorLeft, cbBottom, frontDoorLeft + doorDrawWidth, cbBottom, "VA: ${state.aberturaPorta}", isHoriz = true, offsetPx = 32.dp.toPx(), orangeTextStyle)

        // Identification Labels
        val cabineLabel = textMeasurer.measure("CABINE", TextStyle(color = Color(0xFF64FFDA), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp))
        drawText(cabineLabel, topLeft = Offset(cbCenterX - cabineLabel.size.width/2f, cbCenterY - 14.dp.toPx()))

        val driveLabelText = if (isElectric) "C.P." else "PISTÃO"
        val driveLabel = textMeasurer.measure(driveLabelText, TextStyle(color = if (isElectric) accentOrange else hydraulicBlue, fontSize = 8.sp, fontWeight = FontWeight.Bold))
        drawText(driveLabel, topLeft = Offset(driveCenterX - driveLabel.size.width/2f, driveCenterY - driveLabel.size.height/2f))

        // 11. Technical Title Block (Carimbo Arquitetônico Informativo)
        val tbWidth = 158.dp.toPx()
        val tbHeight = 52.dp.toPx()
        val tbLeft = canvasWidth - tbWidth - 8.dp.toPx()
        val tbTop = canvasHeight - tbHeight - 8.dp.toPx()

        drawRect(
            color = Color(0xEE0A192F),
            topLeft = Offset(tbLeft, tbTop),
            size = Size(tbWidth, tbHeight)
        )
        drawRect(
            color = Color(0xFF64FFDA),
            topLeft = Offset(tbLeft, tbTop),
            size = Size(tbWidth, tbHeight),
            style = Stroke(width = 1f)
        )

        val tbTitle = textMeasurer.measure("PROJETO EXECUTIVO", TextStyle(color = Color(0xFF64FFDA), fontSize = 8.5.sp, fontWeight = FontWeight.Bold))
        drawText(tbTitle, topLeft = Offset(tbLeft + 6.dp.toPx(), tbTop + 4.dp.toPx()))

        val tbDetail1 = textMeasurer.measure("${state.acionamento.uppercase()} | ARCADA: ${state.arcada}", TextStyle(color = Color.White, fontSize = 7.sp))
        drawText(tbDetail1, topLeft = Offset(tbLeft + 6.dp.toPx(), tbTop + 16.dp.toPx()))

        val tbDetail2 = textMeasurer.measure("ENTRADA: ${state.tipoEntrada.uppercase()}", TextStyle(color = Color(0xCCFFFFFF), fontSize = 7.sp))
        drawText(tbDetail2, topLeft = Offset(tbLeft + 6.dp.toPx(), tbTop + 27.dp.toPx()))

        val tbDetail3 = textMeasurer.measure("PORTA: ${state.tipoPorta} (${state.aberturaPorta}mm)", TextStyle(color = accentOrange, fontSize = 6.8.sp))
        drawText(tbDetail3, topLeft = Offset(tbLeft + 6.dp.toPx(), tbTop + 38.dp.toPx()))

        // Header Title
        val header = textMeasurer.measure("ESBOÇO DO POÇO (PLANTA BAIXA)", TextStyle(color = Color(0xFF90CDF4), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
        drawText(header, topLeft = Offset(12.dp.toPx(), 12.dp.toPx()))
    }
}

// Sub-drawing helpers
private fun DrawScope.drawCounterweight(x: Float, y: Float, width: Float, height: Float, color: Color) {
    drawRect(color = color, topLeft = Offset(x, y), size = Size(width, height), style = Stroke(width = 2f))
    // Internal weight blocks
    val divisions = 3
    val step = height / divisions
    for (i in 1 until divisions) {
        drawLine(color = color.copy(alpha = 0.6f), start = Offset(x, y + i * step), end = Offset(x + width, y + i * step), strokeWidth = 1f)
    }
    // Diagonal cross
    drawLine(color = color.copy(alpha = 0.8f), start = Offset(x, y), end = Offset(x + width, y + height), strokeWidth = 1.2f)
    drawLine(color = color.copy(alpha = 0.8f), start = Offset(x + width, y), end = Offset(x, y + height), strokeWidth = 1.2f)
}

private fun DrawScope.drawHydraulicPiston(centerX: Float, centerY: Float, radius: Float, color: Color) {
    // Outer cylinder
    drawCircle(color = color, radius = radius, center = Offset(centerX, centerY), style = Stroke(width = 2f))
    // Inner ram / piston
    drawCircle(color = color.copy(alpha = 0.5f), radius = radius * 0.6f, center = Offset(centerX, centerY), style = Stroke(width = 1.5f))
    // Center point
    drawCircle(color = color, radius = 2.dp.toPx(), center = Offset(centerX, centerY))
    // Support flange
    drawLine(color = color, start = Offset(centerX - radius * 1.3f, centerY), end = Offset(centerX + radius * 1.3f, centerY), strokeWidth = 1.5f)
}

private fun DrawScope.drawGuideRail(x: Float, y: Float, angle: Float, color: Color) {
    val rw = 5.dp.toPx()
    val rh = 7.dp.toPx()
    withTransform({
        translate(left = x, top = y)
        rotate(degrees = angle)
    }) {
        // T-shaped guide rail
        drawRect(color = color, topLeft = Offset(-rw / 2f, 0f), size = Size(rw, rh))
        drawRect(color = color, topLeft = Offset(-rw, rh), size = Size(rw * 2f, rh / 2.2f))
    }
}

private fun DrawScope.drawSlidingDoors(
    doorType: String,
    doorLeft: Float,
    doorWidth: Float,
    cabineY: Float,
    landingY: Float,
    isVertical: Boolean,
    primaryColor: Color,
    secondaryColor: Color
) {
    val numLeaves = when {
        doorType.contains("4 Folhas") -> 4
        doorType.contains("3 Folhas") -> 3
        else -> 2
    }
    val isCentral = doorType.contains("Central")

    if (!isVertical) {
        // Horizontal doors (Front / Rear)
        if (isCentral) {
            // Central opening: leaves part from center
            val halfWidth = doorWidth / 2f
            val leafWidth = halfWidth / (numLeaves / 2f)
            
            for (i in 0 until (numLeaves / 2)) {
                val stepOffset = i * 2.5.dp.toPx()
                // Left half leaf
                val lx1 = doorLeft + (i * leafWidth)
                val lx2 = lx1 + leafWidth - 1.5.dp.toPx()
                // Cabin leaf
                drawLine(color = primaryColor, start = Offset(lx1, cabineY + 2.dp.toPx() + stepOffset), end = Offset(lx2, cabineY + 2.dp.toPx() + stepOffset), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
                // Landing leaf
                drawLine(color = secondaryColor, start = Offset(lx1, landingY + stepOffset), end = Offset(lx2, landingY + stepOffset), strokeWidth = 2.dp.toPx())

                // Right half leaf
                val rx2 = doorLeft + doorWidth - (i * leafWidth)
                val rx1 = rx2 - leafWidth + 1.5.dp.toPx()
                // Cabin leaf
                drawLine(color = primaryColor, start = Offset(rx1, cabineY + 2.dp.toPx() + stepOffset), end = Offset(rx2, cabineY + 2.dp.toPx() + stepOffset), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
                // Landing leaf
                drawLine(color = secondaryColor, start = Offset(rx1, landingY + stepOffset), end = Offset(rx2, landingY + stepOffset), strokeWidth = 2.dp.toPx())
            }
        } else {
            // Lateral opening: leaves slide towards one side
            val leafWidth = doorWidth / numLeaves
            for (i in 0 until numLeaves) {
                val stepOffset = i * 2.5.dp.toPx()
                val x1 = doorLeft + (i * leafWidth)
                val x2 = x1 + leafWidth - 1.5.dp.toPx()
                // Cabin leaf
                drawLine(color = primaryColor, start = Offset(x1, cabineY + 2.dp.toPx() + stepOffset), end = Offset(x2, cabineY + 2.dp.toPx() + stepOffset), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
                // Landing leaf
                drawLine(color = secondaryColor, start = Offset(x1, landingY + stepOffset), end = Offset(x2, landingY + stepOffset), strokeWidth = 2.dp.toPx())
            }
        }
    } else {
        // Vertical doors (Adjacent side entrance)
        val leafHeight = doorWidth / numLeaves
        for (i in 0 until numLeaves) {
            val stepOffset = i * 2.5.dp.toPx()
            val y1 = doorLeft + (i * leafHeight)
            val y2 = y1 + leafHeight - 1.5.dp.toPx()
            // Cabin leaf
            drawLine(color = primaryColor, start = Offset(cabineY + 2.dp.toPx() + stepOffset, y1), end = Offset(cabineY + 2.dp.toPx() + stepOffset, y2), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
            // Landing leaf
            drawLine(color = secondaryColor, start = Offset(landingY + stepOffset, y1), end = Offset(landingY + stepOffset, y2), strokeWidth = 2.dp.toPx())
        }
    }
}
