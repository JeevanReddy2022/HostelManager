package com.example.hostelmanager.ui.expenses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hostelmanager.data.Expense
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppGreen
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppSoftBlue
import com.example.hostelmanager.ui.theme.AppSoftGreen
import com.example.hostelmanager.ui.theme.AppSoftRed
import com.example.hostelmanager.ui.theme.AppText
import java.io.File
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private enum class ExpensePicker { MONTH, CATEGORY, SORT }

/* Category identity — each expense type carries its own hue and glyph. */
private data class CategoryStyle(val accent: Color, val soft: Color, val icon: ImageVector)

private fun categoryStyle(category: String): CategoryStyle = when (category) {
    "Electricity" -> CategoryStyle(Color(0xFFF59E0B), Color(0xFFFEF3C7), Icons.Default.Bolt)
    "Water" -> CategoryStyle(Color(0xFF0EA5E9), Color(0xFFE0F2FE), Icons.Default.WaterDrop)
    "Maintenance" -> CategoryStyle(Color(0xFF8B5CF6), Color(0xFFEDE9FE), Icons.Default.Build)
    "Repairs" -> CategoryStyle(AppRed, AppSoftRed, Icons.Default.Handyman)
    "Supplies" -> CategoryStyle(AppGreen, AppSoftGreen, Icons.Default.ShoppingCart)
    "Salary" -> CategoryStyle(AppNavy, AppSoftBlue, Icons.Default.Payments)
    else -> CategoryStyle(Color(0xFF64748B), Color(0xFFF1F5F9), Icons.Default.Receipt)
}

@Composable
fun ExpensesListScreen(
    hostelId: String,
    onAddExpenseClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpensesViewModel = viewModel()
) {
    LaunchedEffect(hostelId) { viewModel.setHostelId(hostelId) }

    val allExpenses by viewModel.expenses.collectAsState()
    val filters by viewModel.filters.collectAsState()

    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }
    var activePicker by remember { mutableStateOf<ExpensePicker?>(null) }

    val visibleExpenses = remember(allExpenses, filters) {
        filterAndSortExpenses(allExpenses, filters)
    }
    val visibleTotal = remember(visibleExpenses) { visibleExpenses.sumOf { it.amountPaise } }

    val months = remember(allExpenses) {
        allExpenses.map { monthKeyOf(it.expenseDate) }
            .filter { it.isNotBlank() }
            .distinct()
            .sortedDescending()
    }
    val categories = remember(allExpenses) {
        allExpenses.map { it.category }.distinct().sorted()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            if (allExpenses.isNotEmpty()) {
                ExpenseControlDeck(
                    filters = filters,
                    resultCount = visibleExpenses.size,
                    totalCount = allExpenses.size,
                    visibleTotalPaise = visibleTotal,
                    onQueryChange = viewModel::setQuery,
                    onOpenPicker = { activePicker = it },
                    onClearAll = viewModel::clearFilters
                )
            }

            when {
                allExpenses.isEmpty() -> EmptyExpenseList()

                visibleExpenses.isEmpty() -> NoMatchingExpenses(
                    query = filters.query,
                    onClearAll = viewModel::clearFilters
                )

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 96.dp)
                ) {
                    items(visibleExpenses, key = { it.id }) { expense ->
                        ExpenseCard(
                            expense = expense,
                            onEditClick = { expenseToEdit = expense },
                            onDeleteClick = { expenseToDelete = expense },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddExpenseClick,
            shape = RoundedCornerShape(18.dp),
            containerColor = AppNavy,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Expense")
        }
    }

    /* ---------------- Delete confirmation ---------------- */

    expenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = AppCard,
            titleContentColor = AppText,
            textContentColor = AppMuted,
            title = { Text("Delete Expense?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Delete ${expense.category} expense of ${formatRupees(expense.amountPaise)} " +
                            "from ${formatDisplayDate(expense.expenseDate)}? This can't be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExpense(expense.id)
                        expenseToDelete = null
                    }
                ) { Text("Delete", color = AppRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Cancel", color = AppMuted)
                }
            }
        )
    }

    /* ---------------- Edit ---------------- */

    expenseToEdit?.let { expense ->
        EditExpenseDialog(
            expense = expense,
            hostelId = hostelId,
            onDismiss = { expenseToEdit = null },
            onUpdate = { id, expDate, entDate, category, amountPaise, note, imagePath ->
                viewModel.updateExpense(id, hostelId, expDate, entDate, category, amountPaise, note, imagePath)
                expenseToEdit = null
            }
        )
    }

    /* ---------------- Pickers ---------------- */

    when (activePicker) {
        ExpensePicker.MONTH -> {
            val options = buildList {
                add(
                    PickerOption(
                        label = "All months",
                        caption = "${allExpenses.size} entries · ${formatRupees(allExpenses.sumOf { it.amountPaise })}",
                        trailing = "${allExpenses.size}",
                        selected = filters.month == null,
                        onSelect = { viewModel.setMonth(null) }
                    )
                )
                months.forEach { key ->
                    val inMonth = allExpenses.filter { monthKeyOf(it.expenseDate) == key }
                    add(
                        PickerOption(
                            label = formatExpenseMonth(key),
                            caption = "${inMonth.size} entries · ${formatRupees(inMonth.sumOf { it.amountPaise })}",
                            trailing = "${inMonth.size}",
                            selected = filters.month == key,
                            onSelect = { viewModel.setMonth(key) }
                        )
                    )
                }
            }
            ExpensePickerDialog(
                title = "Month",
                subtitle = "Filter by when the expense occurred",
                icon = Icons.Default.CalendarMonth,
                options = options,
                onDismiss = { activePicker = null }
            )
        }

        ExpensePicker.CATEGORY -> {
            val options = buildList {
                add(
                    PickerOption(
                        label = "All categories",
                        caption = "${allExpenses.size} entries · ${formatRupees(allExpenses.sumOf { it.amountPaise })}",
                        trailing = "${allExpenses.size}",
                        selected = filters.category == null,
                        onSelect = { viewModel.setCategory(null) }
                    )
                )
                categories.forEach { cat ->
                    val inCat = allExpenses.filter { it.category == cat }
                    add(
                        PickerOption(
                            label = cat,
                            caption = "${inCat.size} entries · ${formatRupees(inCat.sumOf { it.amountPaise })}",
                            trailing = "${inCat.size}",
                            selected = filters.category == cat,
                            onSelect = { viewModel.setCategory(cat) }
                        )
                    )
                }
            }
            ExpensePickerDialog(
                title = "Category",
                subtitle = "Filter by expense type",
                icon = Icons.Default.Category,
                options = options,
                onDismiss = { activePicker = null }
            )
        }

        ExpensePicker.SORT -> {
            val options = ExpenseSort.entries.map { option ->
                PickerOption(
                    label = option.label,
                    caption = option.caption,
                    trailing = option.short,
                    selected = filters.sort == option,
                    onSelect = { viewModel.setSort(option) }
                )
            }
            ExpensePickerDialog(
                title = "Sort expenses",
                subtitle = "Choose the order of the list",
                icon = Icons.Default.SwapVert,
                options = options,
                onDismiss = { activePicker = null }
            )
        }

        null -> Unit
    }
}

/* ------------------------------------------------------------------ */
/*  Control deck — search, month, category, sort                       */
/* ------------------------------------------------------------------ */

@Composable
private fun ExpenseControlDeck(
    filters: ExpenseFilters,
    resultCount: Int,
    totalCount: Int,
    visibleTotalPaise: Long,
    onQueryChange: (String) -> Unit,
    onOpenPicker: (ExpensePicker) -> Unit,
    onClearAll: () -> Unit
) {
    Surface(
        color = AppCard,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = filters.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Search note or category…", color = AppMuted, maxLines = 1) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (filters.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = AppMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppText,
                        unfocusedTextColor = AppText,
                        focusedBorderColor = AppNavy,
                        unfocusedBorderColor = AppBorder,
                        focusedContainerColor = AppBackground,
                        unfocusedContainerColor = AppBackground,
                        cursorColor = AppNavy,
                        focusedLeadingIconColor = AppNavy,
                        unfocusedLeadingIconColor = AppMuted
                    )
                )

                val sortChanged = filters.sort != ExpenseSort.DATE_DESC
                Surface(
                    onClick = { onOpenPicker(ExpensePicker.SORT) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (sortChanged) AppNavy else AppBackground,
                    contentColor = if (sortChanged) Color.White else AppText,
                    border = if (sortChanged) null else BorderStroke(1.dp, AppBorder),
                    modifier = Modifier
                        .width(70.dp)
                        .height(56.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.SwapVert,
                            contentDescription = "Sort expenses",
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            filters.sort.short,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    label = filters.month?.let { formatExpenseMonth(it) } ?: "All months",
                    icon = Icons.Default.CalendarMonth,
                    active = filters.month != null,
                    onClick = { onOpenPicker(ExpensePicker.MONTH) }
                )
                FilterChip(
                    label = filters.category ?: "All categories",
                    icon = Icons.Default.Category,
                    active = filters.category != null,
                    onClick = { onOpenPicker(ExpensePicker.CATEGORY) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$resultCount of $totalCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(AppSoftGreen)
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        formatRupees(visibleTotalPaise),
                        fontSize = 11.sp,
                        color = AppGreen,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (filters.isFiltered) {
                    Surface(
                        onClick = onClearAll,
                        shape = RoundedCornerShape(50),
                        color = AppSoftRed,
                        contentColor = AppRed
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                "Clear ${filters.activeCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (active) AppNavy else AppBackground,
        contentColor = if (active) Color.White else AppText,
        border = if (active) null else BorderStroke(1.dp, AppBorder)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Expense card                                                       */
/* ------------------------------------------------------------------ */

@Composable
fun ExpenseCard(
    expense: Expense,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = categoryStyle(expense.category)
    val hasReceipt = !expense.receiptImagePath.isNullOrBlank()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        border = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Receipt thumbnail when available, otherwise the category glyph
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(style.soft),
                contentAlignment = Alignment.Center
            ) {
                if (hasReceipt) {
                    AsyncImage(
                        model = File(expense.receiptImagePath!!),
                        contentDescription = "Receipt",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = style.icon,
                        contentDescription = null,
                        tint = style.accent,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    expense.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(style.soft)
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        formatDisplayDate(expense.expenseDate),
                        fontSize = 11.sp,
                        color = style.accent,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                if (expense.enteredDate != expense.expenseDate) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Recorded ${formatDisplayDate(expense.enteredDate)}",
                        fontSize = 11.sp,
                        color = AppMuted,
                        maxLines = 1
                    )
                }

                if (expense.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        expense.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatRupees(expense.amountPaise),
                    fontSize = 19.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = style.accent,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(38.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = AppNavy,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(38.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = AppRed,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Picker dialog                                                      */
/* ------------------------------------------------------------------ */

private data class PickerOption(
    val label: String,
    val caption: String,
    val trailing: String,
    val selected: Boolean,
    val onSelect: () -> Unit
)

@Composable
private fun ExpensePickerDialog(
    title: String,
    subtitle: String,
    icon: ImageVector,
    options: List<PickerOption>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppCard,
        titleContentColor = AppText,
        textContentColor = AppText,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AppSoftBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = AppNavy,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(options) { option ->
                    Surface(
                        onClick = {
                            option.onSelect()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (option.selected) AppSoftBlue else AppBackground,
                        contentColor = AppText,
                        border = BorderStroke(
                            1.dp,
                            if (option.selected) AppNavy.copy(alpha = 0.35f) else AppBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    option.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (option.selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (option.selected) AppNavy else AppText
                                )
                                Text(
                                    option.caption,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppMuted
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (option.selected) AppNavy else AppBorder)
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    option.trailing,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (option.selected) Color.White else AppMuted
                                )
                            }
                            if (option.selected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = AppNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = AppNavy, fontWeight = FontWeight.Bold)
            }
        }
    )
}

/* ------------------------------------------------------------------ */
/*  Empty states                                                       */
/* ------------------------------------------------------------------ */

@Composable
private fun EmptyExpenseList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AppSoftBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Receipt,
                contentDescription = null,
                tint = AppNavy,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No expenses recorded yet.",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Tap the + button to add your first expense.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppMuted
        )
    }
}

@Composable
private fun NoMatchingExpenses(query: String, onClearAll: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AppSoftRed),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                tint = AppRed,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No expenses match",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            if (query.isNotBlank())
                "Nothing found for “$query” with the current month and category."
            else "Try a different month or category.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        Surface(
            onClick = onClearAll,
            shape = RoundedCornerShape(14.dp),
            color = AppNavy,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear all filters", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Helpers                                                            */
/* ------------------------------------------------------------------ */

private fun formatRupees(amountPaise: Long): String {
    val rupees = amountPaise / 100
    val formatted = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(rupees)
    return "₹$formatted"
}

private fun formatDisplayDate(iso: String): String {
    val date = runCatching { LocalDate.parse(iso) }.getOrNull() ?: return iso
    val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    return "${date.dayOfMonth} $month ${date.year}"
}