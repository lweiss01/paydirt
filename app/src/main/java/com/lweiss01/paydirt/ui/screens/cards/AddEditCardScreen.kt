package com.lweiss01.paydirt.ui.screens.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lweiss01.paydirt.ui.theme.PayDirtColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardScreen(
    cardId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddEditCardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val isEditing = cardId != null

    LaunchedEffect(cardId) {
        if (cardId != null) viewModel.loadCard(cardId)
    }

    Scaffold(
        containerColor = PayDirtColors.Background,
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Card" else "Add Card", color = PayDirtColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PayDirtColors.TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PayDirtColors.Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("No account numbers needed.", style = MaterialTheme.typography.bodyMedium)

            PayDirtTextField(label = "Card Name", value = state.name, onValueChange = viewModel::setName, placeholder = "Chase Sapphire, Citi Double Cash…")
            PayDirtTextField(label = "Current Balance ($)", value = state.balance, onValueChange = viewModel::setBalance, placeholder = "2500", isNumeric = true)
            PayDirtTextField(label = "APR (%)", value = state.apr, onValueChange = viewModel::setApr, placeholder = "22.99", isNumeric = true)

            Column {
                PayDirtTextField(label = "Minimum Payment ($)", value = state.minPayment, onValueChange = viewModel::setMinPayment, placeholder = "50", isNumeric = true)
                if (state.estimatedMinPayment.isNotEmpty() && state.minPayment.isEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    TextButton(
                        onClick = viewModel::useEstimatedMin,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Use estimated minimum: $${state.estimatedMinPayment}/mo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PayDirtColors.Primary
                        )
                    }
                }
            }

            // Color picker
            Column {
                Text("Card Color", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PayDirtColors.CardColors.forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (state.colorTag == index)
                                        Modifier.border(3.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                                .clickable { viewModel.setColorTag(index) }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch {
                        val saved = viewModel.save(cardId)
                        if (saved) {
                            withContext(Dispatchers.Main.immediate) {
                                onSaved()
                            }
                        }
                    }
                },
                enabled = state.isValid && !state.isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PayDirtColors.Primary)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text(if (isEditing) "Save Changes" else "Add Card", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun PayDirtTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isNumeric: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = PayDirtColors.TextDisabled) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = if (isNumeric) KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done) else KeyboardOptions.Default,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PayDirtColors.Primary,
            unfocusedBorderColor = PayDirtColors.Border,
            focusedLabelColor = PayDirtColors.Primary,
            unfocusedLabelColor = PayDirtColors.TextMuted,
            focusedTextColor = PayDirtColors.TextPrimary,
            unfocusedTextColor = PayDirtColors.TextPrimary,
            cursorColor = PayDirtColors.Primary
        ),
        shape = RoundedCornerShape(10.dp)
    )
}
