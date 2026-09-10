package com.example

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ElevatorConfigViewModel : ViewModel() {
    
    val acionamentos = listOf("Elétrico", "Hidráulico")
    val arcadas = listOf("L", "Suspensão")
    val posicoes = listOf("Centralizado", "Deslocado")
    val tiposPorta = listOf("2 Folhas Lateral", "3 Folhas Lateral", "2 Folhas Central", "4 Folhas Central")
    val tiposEntrada = listOf("Unilateral", "Oposta", "Adjacente")
    val ladosArcada = listOf("Lateral", "Fundo")
    val aberturasPorta = listOf("700", "800", "900", "1000", "1100", "1200")

    private val _uiState = MutableStateFlow(ElevatorConfigState())
    val uiState = _uiState.asStateFlow()

    fun updateLarguraCabine(value: String) {
        _uiState.update { it.copy(larguraCabine = value) }
        calculateResults()
    }
    
    fun updateProfundidadeCabine(value: String) {
        _uiState.update { it.copy(profundidadeCabine = value) }
        calculateResults()
    }

    fun updateAberturaPorta(value: String) {
        _uiState.update { it.copy(aberturaPorta = value) }
        calculateResults()
    }

    fun updateAcionamento(value: String) {
        _uiState.update { it.copy(acionamento = value) }
        calculateResults()
    }

    fun updateArcada(value: String) {
        _uiState.update { it.copy(arcada = value) }
        calculateResults()
    }

    fun updatePosicao(value: String) {
        _uiState.update { it.copy(posicao = value) }
        calculateResults()
    }

    fun updateTipoPorta(value: String) {
        _uiState.update { it.copy(tipoPorta = value) }
        calculateResults()
    }

    fun updateTipoEntrada(value: String) {
        _uiState.update { it.copy(tipoEntrada = value) }
        calculateResults()
    }

    fun updateLadoArcada(value: String) {
        _uiState.update { it.copy(ladoArcada = value) }
        calculateResults()
    }

    private fun calculateResults() {
        val state = _uiState.value
        val largura = state.larguraCabine.toIntOrNull() ?: 800
        val profundidade = state.profundidadeCabine.toIntOrNull() ?: 1250
        val abertura = state.aberturaPorta.toIntOrNull() ?: 800
        
        val indexAbertura = aberturasPorta.indexOf(state.aberturaPorta).takeIf { it >= 0 } ?: 1
        
        var folgaPorta = when (state.tipoPorta) {
            "2 Folhas Lateral" -> 400 + (indexAbertura * 50)
            "3 Folhas Lateral" -> 260 + (indexAbertura * 40)
            "2 Folhas Central" -> 350 + (indexAbertura * 45)
            "4 Folhas Central" -> 220 + (indexAbertura * 35)
            else -> 400
        }
        
        var espacoChassis = when {
            state.acionamento == "Hidráulico" && state.arcada == "L" -> 320
            state.acionamento == "Hidráulico" -> 280
            state.arcada == "L" -> 450
            state.ladoArcada == "Lateral" -> 420
            else -> 400
        }

        val menorFolga = 125
        val folgaFrontal = 160
        val folgaFundoPadrao = 120
        
        // Additional clearances for multiple entrances
        val folgaEntradaOposta = if (state.tipoEntrada == "Oposta") 160 else 0
        val folgaEntradaAdjacente = if (state.tipoEntrada == "Adjacente") 160 else 0
        
        val larguraPoco = if (state.ladoArcada == "Lateral") {
            largura + espacoChassis + menorFolga + folgaEntradaAdjacente
        } else {
            largura + (menorFolga * 2) + folgaEntradaAdjacente
        }
        
        val profundidadePoco = if (state.ladoArcada == "Fundo") {
            profundidade + folgaFrontal + espacoChassis + folgaEntradaOposta
        } else {
            profundidade + folgaFrontal + folgaFundoPadrao + folgaEntradaOposta
        }
        
        val tipoSuporte = if (state.arcada == "Suspensão") "Braket Suspensão" else "Suporte Guia Tipo L"
        val qtdSuporte = if (state.arcada == "Suspensão") 4 else 6

        _uiState.update { 
            it.copy(
                larguraPoco = larguraPoco.toString(),
                profundidadePoco = profundidadePoco.toString(),
                folgaPorta = folgaPorta.toString(),
                espacoChassis = espacoChassis.toString(),
                tipoSuporte = tipoSuporte,
                qtdSuporte = qtdSuporte.toString(),
                menorFolga = menorFolga.toString()
            ) 
        }
    }
}

data class ElevatorConfigState(
    val larguraCabine: String = "800",
    val profundidadeCabine: String = "1250",
    val aberturaPorta: String = "800",
    val acionamento: String = "Elétrico",
    val arcada: String = "L",
    val posicao: String = "Centralizado",
    val tipoPorta: String = "2 Folhas Lateral",
    val tipoEntrada: String = "Unilateral",
    val ladoArcada: String = "Lateral",
    
    // Resultados
    val larguraPoco: String = "1700",
    val profundidadePoco: String = "1545",
    val folgaPorta: String = "450",
    val espacoChassis: String = "450",
    val tipoSuporte: String = "Suporte T",
    val qtdSuporte: String = "6",
    val menorFolga: String = "125"
)
