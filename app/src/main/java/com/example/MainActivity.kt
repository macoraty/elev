package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ElevatorConfigViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = { Text("Configurador de Elevadores", fontWeight = FontWeight.Bold) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                ) { innerPadding ->
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    ElevatorConfigScreen(
                        state = state,
                        viewModel = viewModel,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun ElevatorConfigScreen(
    state: ElevatorConfigState,
    viewModel: ElevatorConfigViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        SectionCard(
            title = "Dimensões da Cabine",
            icon = { Icon(Icons.Default.Architecture, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = state.larguraCabine,
                    onValueChange = viewModel::updateLarguraCabine,
                    label = { Text("Largura (mm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.profundidadeCabine,
                    onValueChange = viewModel::updateProfundidadeCabine,
                    label = { Text("Profund. (mm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            DropdownField(
                label = "Abertura da Porta (mm)",
                options = viewModel.aberturasPorta,
                selectedOption = state.aberturaPorta,
                onOptionSelected = viewModel::updateAberturaPorta
            )
        }

        SectionCard(
            title = "Configuração do Equipamento",
            icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        ) {
            DropdownField(
                label = "Acionamento",
                options = viewModel.acionamentos,
                selectedOption = state.acionamento,
                onOptionSelected = viewModel::updateAcionamento
            )
            DropdownField(
                label = "Tipo de Arcada",
                options = viewModel.arcadas,
                selectedOption = state.arcada,
                onOptionSelected = viewModel::updateArcada
            )
            DropdownField(
                label = "Posição (Contrapeso/Máquina)",
                options = viewModel.posicoes,
                selectedOption = state.posicao,
                onOptionSelected = viewModel::updatePosicao
            )
            DropdownField(
                label = "Tipo de Porta",
                options = viewModel.tiposPorta,
                selectedOption = state.tipoPorta,
                onOptionSelected = viewModel::updateTipoPorta
            )
            DropdownField(
                label = "Tipo de Entrada",
                options = viewModel.tiposEntrada,
                selectedOption = state.tipoEntrada,
                onOptionSelected = viewModel::updateTipoEntrada
            )
            DropdownField(
                label = "Lado da Arcada / Chassis",
                options = viewModel.ladosArcada,
                selectedOption = state.ladoArcada,
                onOptionSelected = viewModel::updateLadoArcada
            )
        }

        SectionCard(
            title = "Resultados do Dimensionamento",
            icon = { Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            ResultRow("Largura do Poço", "${state.larguraPoco} mm", isHighlight = true)
            ResultRow("Profundidade do Poço", "${state.profundidadePoco} mm", isHighlight = true)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
            ResultRow("Folga Necessária (Porta)", "${state.folgaPorta} mm")
            ResultRow("Espaço Chassis (L/S)", "${state.espacoChassis} mm")
            ResultRow("Menor Folga", "${state.menorFolga} mm")
            ResultRow("Tipo de Suporte", state.tipoSuporte)
            ResultRow("Quantidade por Andar", state.qtdSuporte)
        }

        SectionCard(
            title = "Esboço do Poço (Blueprint)",
            icon = { Icon(Icons.Default.Architecture, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        ) {
            BlueprintDrawing(
                state = state,
                modifier = Modifier.testTag("blueprint_drawing")
            )
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: @Composable () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icon()
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (containerColor == MaterialTheme.colorScheme.secondaryContainer) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ResultRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label, 
            style = if (isHighlight) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = value, 
            style = if (isHighlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
