package com.kizvpn.admin.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kizvpn.admin.data.model.QRPaymentData
import com.kizvpn.admin.data.model.Tariff
import com.kizvpn.admin.di.ViewModelFactory
import com.kizvpn.admin.ui.components.StatCard
import com.kizvpn.admin.ui.viewmodel.PaymentsViewModel
import com.kizvpn.admin.util.QRCodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    onBack: () -> Unit = {},
    viewModelFactory: ViewModelFactory
) {
    val viewModel: com.kizvpn.admin.ui.viewmodel.PaymentsViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val filteredPayments by viewModel.filteredPayments.collectAsState()
    
    var showQRDialog by remember { mutableStateOf<QRPaymentData?>(null) }
    var selectedTariff by remember { mutableStateOf<Tariff?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Платежи") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "Обновить")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Статистика
            if (uiState.totalRevenue > 0 || uiState.todayRevenue > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Всего выручки",
                        value = "${uiState.totalRevenue.toInt()}₽",
                        subtitle = "Все платежи",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Сегодня",
                        value = "${uiState.todayRevenue.toInt()}₽",
                        subtitle = "За сегодня",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Кнопка генерации QR
            Button(
                onClick = {
                    // Показываем список тарифов для выбора
                    selectedTariff = Tariff(
                        id = 0,
                        name = "Тестовый",
                        description = null,
                        durationDays = 30,
                        trafficLimitGb = 100,
                        price = 100.0,
                        protocol = "vless",
                        isActive = true
                    )
                    showQRDialog = QRPaymentData(
                        cardNumber = "2202206815876342",
                        amount = selectedTariff!!.price,
                        tariffName = selectedTariff!!.name
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.QrCode, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Сгенерировать QR-код для оплаты")
            }
            
            // Фильтры
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedStatus == null,
                    onClick = { viewModel.loadPayments(null) },
                    label = { Text("Все") }
                )
                FilterChip(
                    selected = uiState.selectedStatus == "pending",
                    onClick = { viewModel.loadPayments("pending") },
                    label = { Text("Ожидает") }
                )
                FilterChip(
                    selected = uiState.selectedStatus == "paid",
                    onClick = { viewModel.loadPayments("paid") },
                    label = { Text("Оплачено") }
                )
            }
            
            // Поиск
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.searchPayments(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Поиск по ID платежа...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredPayments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Платежи не найдены",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Список платежей будет загружен когда будет готов API endpoint",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPayments) { payment ->
                        PaymentCard(payment = payment)
                    }
                }
            }
        }
    }
    
    // Диалог с QR-кодом
    showQRDialog?.let { qrData ->
        AlertDialog(
            onDismissRequest = { showQRDialog = null },
            title = { Text("QR-код для оплаты") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Тариф: ${qrData.tariffName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Сумма: ${qrData.amount}₽",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Карта: ${qrData.cardNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val qrBitmap = QRCodeGenerator.generateSberbankQR(qrData)
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR код для оплаты",
                        modifier = Modifier.size(256.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showQRDialog = null }) {
                    Text("Закрыть")
                }
            }
        )
    }
}

@Composable
fun PaymentCard(payment: com.kizvpn.admin.data.model.Payment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Платеж #${payment.id}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = payment.paymentId ?: "Без ID",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (payment.status) {
                        "paid" -> MaterialTheme.colorScheme.primaryContainer
                        "pending" -> MaterialTheme.colorScheme.surfaceVariant
                        "failed" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = payment.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Сумма: ${payment.amount} ${payment.currency}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = payment.paymentMethod,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
