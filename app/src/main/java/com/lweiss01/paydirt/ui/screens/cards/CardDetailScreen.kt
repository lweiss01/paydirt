package com.lweiss01.paydirt.ui.screens.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lweiss01.paydirt.domain.engine.BehaviorEngine
import com.lweiss01.paydirt.domain.model.AprSource
import com.lweiss01.paydirt.domain.model.Payment
import com.lweiss01.paydirt.ui.components.APRTrustBadge
import com.lweiss01.paydirt.ui.components.aprTrustCopyForSource
import com.lweiss01.paydirt.ui.theme.PayDirtColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    cardId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onRewardReady: (BehaviorEngine.PaymentImpact, AprSource) -> Unit = { _, _ -> },
    suggestedPaymentAmount: Double? = null,
    onSuggestedPaymentHandled: () -> Unit = {},
    forceClosePaymentSheet: Boolean = false,
    onForceClosePaymentSheetHandled: () -> Unit = {},
    viewModel: CardDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showPaymentSheet by remember { mutableStateOf(false) }
    var paymentSheetDefaultAmount by remember { mutableStateOf(0.0) }

    LaunchedEffect(cardId) { viewModel.loadCard(cardId) }

    LaunchedEffect(viewModel, cardId) {
        viewModel.events.collect { event ->
            when (event) {
                is CardDetailEvent.RewardReady -> if (event.cardId == cardId) {
                    onRewardReady(event.impact, state.card?.aprSource ?: AprSource.UNKNOWN)
                }
            }
        }
    }

    LaunchedEffect(suggestedPaymentAmount) {
        if (suggestedPaymentAmount != null) {
            paymentSheetDefaultAmount = suggestedPaymentAmount
            showPaymentSheet = true
            onSuggestedPaymentHandled()
        }
    }

    LaunchedEffect(forceClosePaymentSheet) {
        if (forceClosePaymentSheet) {
            showPaymentSheet = false
            paymentSheetDefaultAmount = 0.0
            onForceClosePaymentSheetHandled()
        }
    }

    Scaffold(
        containerColor = PayDirtColors.Background,
        topBar = {
            TopAppBar(
                title = { Text(state.card?.name ?: "", color = PayDirtColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PayDirtColors.TextSecondary)
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PayDirtColors.TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PayDirtColors.Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    paymentSheetDefaultAmount = state.card?.minPayment ?: 0.0
                    showPaymentSheet = true
                },
                containerColor = PayDirtColors.Win,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.testTag("card_detail_log_payment_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Payment")
            }
        }
    ) { padding ->
        val card = state.card
        if (card == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PayDirtColors.Primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val cardColor = PayDirtColors.CardColors.getOrElse(card.colorTag) { PayDirtColors.Primary }
                val progress = if (card.originalBalance > 0)
                    (1.0 - card.currentBalance / card.originalBalance).toFloat().coerceIn(0f, 1f) else 0f
                val aprTrustCopy = aprTrustCopyForSource(card.aprSource)

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PayDirtColors.Surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("CURRENT BALANCE", style = MaterialTheme.typography.labelSmall)
                                Text(formatCurrency(card.currentBalance), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = PayDirtColors.TextPrimary)
                            }
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.widthIn(max = 220.dp)
                            ) {
                                Text("APR CONFIDENCE", style = MaterialTheme.typography.labelSmall)
                                APRTrustBadge(aprSource = card.aprSource)
                                Text(
                                    text = aprTrustCopy.helperText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = PayDirtColors.TextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = cardColor,
                            trackColor = PayDirtColors.Border
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${(progress * 100).toInt()}% paid off", style = MaterialTheme.typography.bodyMedium, fontSize = 12.sp)
                            Text("of ${formatCurrency(card.originalBalance)}", style = MaterialTheme.typography.bodyMedium, fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniStatCard(Modifier.weight(1f), "Min Payment", formatCurrency(card.minPayment) + "/mo", PayDirtColors.TextSecondary)
                    MiniStatCard(Modifier.weight(1f), "Total Paid", formatCurrency(state.totalPaid), PayDirtColors.Win)
                    MiniStatCard(Modifier.weight(1f), "Monthly Int.", formatCurrency(card.currentBalance * card.apr / 100 / 12), PayDirtColors.Danger)
                }
            }

            item {
                Text("PAYMENT HISTORY", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 8.dp))
            }

            if (state.payments.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No payments logged yet.\nTap + to log your first payment.", style = MaterialTheme.typography.bodyMedium, color = PayDirtColors.TextMuted)
                    }
                }
            } else {
                items(state.payments, key = { it.id }) { payment ->
                    PaymentRow(payment)
                }
            }
        }
    }

    if (showPaymentSheet) {
        key(paymentSheetDefaultAmount) {
            LogPaymentSheet(
                defaultAmount = paymentSheetDefaultAmount,
                onDismiss = { showPaymentSheet = false },
                onConfirm = { amount, isExtra, note ->
                    viewModel.logPayment(cardId, amount, isExtra, note)
                    showPaymentSheet = false
                }
            )
        }
    }
}

@Composable
private fun MiniStatCard(modifier: Modifier, label: String, value: String, color: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = PayDirtColors.Surface)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PaymentRow(payment: Payment) {
    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = PayDirtColors.Surface)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (payment.isExtraPayment) PayDirtColors.Win else PayDirtColors.Primary))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(if (payment.isExtraPayment) "Extra payment" else "Minimum payment", style = MaterialTheme.typography.bodyMedium, color = PayDirtColors.TextPrimary)
                if (!payment.note.isNullOrBlank()) {
                    Text(payment.note, style = MaterialTheme.typography.bodyMedium, fontSize = 12.sp)
                }
                Text(SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(payment.paidAt)), style = MaterialTheme.typography.bodyMedium, fontSize = 12.sp, color = PayDirtColors.TextMuted)
            }
            Text(formatCurrency(payment.amount), style = MaterialTheme.typography.titleMedium, color = PayDirtColors.Win, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogPaymentSheet(
    defaultAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, Boolean, String?) -> Unit
) {
    var amount by remember(defaultAmount) { mutableStateOf("%.0f".format(defaultAmount)) }
    var isExtra by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = PayDirtColors.Surface) {
        Column(modifier = Modifier.padding(20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Log Payment", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = amount, onValueChange = { amount = it },
                label = { Text("Amount ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_detail_payment_amount_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PayDirtColors.Primary, unfocusedBorderColor = PayDirtColors.Border,
                    focusedTextColor = PayDirtColors.TextPrimary, unfocusedTextColor = PayDirtColors.TextPrimary,
                    cursorColor = PayDirtColors.Primary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("This is an extra payment", style = MaterialTheme.typography.titleMedium)
                    Text("Above the minimum", style = MaterialTheme.typography.bodyMedium, fontSize = 12.sp)
                }
                Switch(
                    checked = isExtra,
                    onCheckedChange = { isExtra = it },
                    modifier = Modifier.testTag("card_detail_payment_extra_switch"),
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PayDirtColors.Win)
                )
            }

            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PayDirtColors.Primary, unfocusedBorderColor = PayDirtColors.Border,
                    focusedTextColor = PayDirtColors.TextPrimary, unfocusedTextColor = PayDirtColors.TextPrimary,
                    cursorColor = PayDirtColors.Primary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: return@Button
                    onConfirm(amt, isExtra, note)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("card_detail_payment_submit"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PayDirtColors.Win)
            ) {
                Text("Log Payment", style = MaterialTheme.typography.titleMedium, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatCurrency(amount: Double) =
    NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 }.format(amount)
