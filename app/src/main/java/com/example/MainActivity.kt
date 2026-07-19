@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Counter
import com.example.data.CounterLog
import com.example.data.Folder
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: CounterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    CounterAppScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Preset Colors for folders and counters
val ColorPresets = listOf(
    "#3F51B5" to "Indigo",
    "#2E7D32" to "Green",
    "#EF6C00" to "Orange",
    "#C62828" to "Red",
    "#00838F" to "Teal",
    "#6A1B9A" to "Purple",
    "#AD1457" to "Pink",
    "#455A64" to "Slate Blue"
)

val ExtendedColorPresets = listOf(
    "#3F51B5" to "Indigo",
    "#2E7D32" to "Green",
    "#EF6C00" to "Orange",
    "#C62828" to "Red",
    "#00838F" to "Teal",
    "#6A1B9A" to "Purple",
    "#AD1457" to "Pink",
    "#455A64" to "Slate Blue",
    "#1E88E5" to "Blue",
    "#00ACC1" to "Cyan",
    "#00897B" to "Teal Accent",
    "#43A047" to "Green Accent",
    "#7CB342" to "Light Green",
    "#C0CA33" to "Lime",
    "#FDD835" to "Yellow",
    "#FFB300" to "Amber",
    "#F4511E" to "Deep Orange",
    "#D81B60" to "Vibrant Pink",
    "#8E24AA" to "Medium Purple",
    "#5E35B1" to "Deep Purple",
    "#3949AB" to "Dark Indigo",
    "#039BE5" to "Light Blue",
    "#0059B3" to "Navy",
    "#2E8B57" to "Sea Green",
    "#20B2AA" to "Light Sea Green",
    "#CD5C5C" to "Indian Red",
    "#DA70D6" to "Orchid",
    "#FF1493" to "Deep Pink",
    "#FF7F50" to "Coral",
    "#607D8B" to "Blue Grey"
)

// Preset Icons
val IconPresets = listOf(
    "Folder" to Icons.Default.Folder,
    "FitnessCenter" to Icons.Default.FitnessCenter,
    "LocalDrink" to Icons.Default.LocalDrink,
    "Book" to Icons.Default.Book,
    "Star" to Icons.Default.Star,
    "Favorite" to Icons.Default.Favorite,
    "AttachMoney" to Icons.Default.AttachMoney,
    "Work" to Icons.Default.Work,
    "ShoppingCart" to Icons.Default.ShoppingCart
)

fun getIconByName(name: String): ImageVector {
    return IconPresets.firstOrNull { it.first == name }?.second ?: Icons.Default.Folder
}

fun getColorFromHex(hex: String): Color {
    return try {
        Color(AndroidColor.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFF3F51B5)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CounterAppScreen(
    viewModel: CounterViewModel,
    modifier: Modifier = Modifier
) {
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val counters by viewModel.counters.collectAsStateWithLifecycle()
    val selectedFolderId by viewModel.selectedFolderId.collectAsStateWithLifecycle()

    var showAddFolderDialog by remember { mutableStateOf(false) }
    var folderToEdit by remember { mutableStateOf<Folder?>(null) }
    var showAddCounterDialog by remember { mutableStateOf(false) }
    var counterToEdit by remember { mutableStateOf<Counter?>(null) }
    var counterForLogs by remember { mutableStateOf<Counter?>(null) }
    var qolAdjustCounterData by remember { mutableStateOf<Pair<Counter, Boolean>?>(null) }
    var historyViewTarget by remember { mutableStateOf<Pair<Counter?, Folder?>?>(null) }

    var showMoreDropdown by remember { mutableStateOf(false) }
    var showInfoDropdown by remember { mutableStateOf(false) }

    var showSortDialog by remember { mutableStateOf(false) }
    var selectedSortParam by remember { mutableStateOf<String?>(null) } // "quantity", "createdAt", "lastModified", "name"
    var isReverseSort by remember { mutableStateOf(false) } // default false (biggest to least)
    var isAutosortEnabled by remember { mutableStateOf(false) }
    var autosortIntervalSeconds by remember { mutableStateOf(5) }
    var manualOrderIds by remember { mutableStateOf<List<Long>?>(null) }

    // Helper to calculate sorted IDs
    val performSort: (String, Boolean) -> Unit = { param, reverse ->
        val sorted = when (param) {
            "quantity" -> {
                if (reverse) counters.sortedBy { it.currentValue }
                else counters.sortedByDescending { it.currentValue }
            }
            "createdAt" -> {
                if (reverse) counters.sortedBy { it.createdAt }
                else counters.sortedByDescending { it.createdAt }
            }
            "lastModified" -> {
                if (reverse) counters.sortedBy { it.lastModified }
                else counters.sortedByDescending { it.lastModified }
            }
            "name" -> {
                if (reverse) counters.sortedBy { it.name.lowercase() }
                else counters.sortedByDescending { it.name.lowercase() }
            }
            else -> counters
        }
        manualOrderIds = sorted.map { it.id }
    }

    // Reset sort when folder changes
    LaunchedEffect(selectedFolderId) {
        selectedSortParam = null
        isReverseSort = false
        isAutosortEnabled = false
        manualOrderIds = null
    }

    // Autosort periodic triggers
    LaunchedEffect(isAutosortEnabled, autosortIntervalSeconds, selectedSortParam, isReverseSort, counters) {
        if (isAutosortEnabled && selectedSortParam != null) {
            while (true) {
                performSort(selectedSortParam!!, isReverseSort)
                kotlinx.coroutines.delay(autosortIntervalSeconds * 1000L)
            }
        }
    }

    // List of counters to display
    val displayedCounters = remember(counters, manualOrderIds) {
        if (manualOrderIds == null) {
            counters
        } else {
            val counterMap = counters.associateBy { it.id }
            val ordered = manualOrderIds!!.mapNotNull { counterMap[it] }
            val missing = counters.filter { it.id !in counterMap }
            ordered + missing
        }
    }

    val context = LocalContext.current

    // Aggregate statistics
    val totalCountersCount = counters.size
    val totalAccumulatedClicks = counters.sumOf { it.currentValue }
    val currentFolder = folders.find { it.id == selectedFolderId }

    // Dynamic primary color based on active folder
    val activeColor = currentFolder?.let { getColorFromHex(it.colorHex) } ?: MaterialTheme.colorScheme.primary

    if (showAddCounterDialog) {
        BackHandler {
            showAddCounterDialog = false
        }
        AddEditCounterScreen(
            counter = null,
            folders = folders,
            preselectedFolderId = selectedFolderId,
            onDismiss = { showAddCounterDialog = false },
            onSave = { name, folderId, initVal, step, target, reset, color, note, quickButtons, thresh ->
                viewModel.createCounter(name, folderId, initVal, step, target, reset, color, note, quickButtons, thresh)
                showAddCounterDialog = false
            }
        )
    } else if (counterToEdit != null) {
        val counter = counterToEdit!!
        BackHandler {
            counterToEdit = null
        }
        AddEditCounterScreen(
            counter = counter,
            folders = folders,
            preselectedFolderId = counter.folderId,
            onDismiss = { counterToEdit = null },
            onSave = { name, folderId, initVal, step, target, reset, color, note, quickButtons, thresh ->
                viewModel.updateCounter(
                    counter.copy(
                        name = name,
                        folderId = folderId,
                        currentValue = initVal,
                        stepSize = step,
                        targetValue = target,
                        resetValue = reset,
                        colorHex = color,
                        note = note,
                        quickButtons = quickButtons,
                        historyDividerThreshold = thresh
                    )
                )
                counterToEdit = null
            },
            onDelete = {
                viewModel.deleteCounter(counter)
                counterToEdit = null
            }
        )
    } else if (historyViewTarget != null) {
        val target = historyViewTarget!!
        BackHandler {
            historyViewTarget = null
        }
        FullScreenHistoryView(
            targetCounter = target.first,
            targetFolder = target.second,
            viewModel = viewModel,
            folders = folders,
            allCounters = counters,
            onDismiss = { historyViewTarget = null }
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
            // Modern Polished Top Banner Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, activeColor.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left element: Button with three dots (...)
                    Box {
                        IconButton(
                            onClick = { showMoreDropdown = true },
                            modifier = Modifier.testTag("more_options_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Folder Navigation & Settings",
                                tint = activeColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Left Dropdown Menu: Settings, Divider, Create Folder, Folders List
                        DropdownMenu(
                            expanded = showMoreDropdown,
                            onDismissRequest = { showMoreDropdown = false },
                            modifier = Modifier
                                .width(240.dp)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            // Top item: Settings (Placeholder for now)
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Settings (Placeholder)", fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    showMoreDropdown = false
                                    Toast.makeText(context, "Settings fully configured per counter/folder!", Toast.LENGTH_SHORT).show()
                                }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )

                            // Create Folder Button
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CreateNewFolder,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Create Folder", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                    }
                                },
                                onClick = {
                                    showMoreDropdown = false
                                    showAddFolderDialog = true
                                }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )

                            // Title for Folders List
                            Text(
                                text = "Switch Folders (Hold to Edit)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )

                            // All Counters Option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            viewModel.selectFolder(null)
                                            showMoreDropdown = false
                                        }
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Dashboard,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "All Counters",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selectedFolderId == null) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selectedFolderId == null) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Custom Folders Switch list
                            folders.forEach { folder ->
                                val isSelected = selectedFolderId == folder.id
                                val fColor = getColorFromHex(folder.colorHex)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {
                                                viewModel.selectFolder(folder.id)
                                                showMoreDropdown = false
                                            },
                                            onLongClick = {
                                                folderToEdit = folder
                                                showMoreDropdown = false
                                            }
                                        )
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(fColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getIconByName(folder.iconName),
                                            contentDescription = null,
                                            tint = fColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = folder.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = fColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Center Element: Name of the Folder (with icon)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(activeColor.copy(alpha = 0.12f))
                                .then(
                                    if (currentFolder != null) {
                                        Modifier.clickable {
                                            historyViewTarget = Pair(null, currentFolder)
                                        }
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (currentFolder != null) getIconByName(currentFolder.iconName) else Icons.Default.Dashboard,
                                contentDescription = null,
                                tint = activeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentFolder?.name ?: "All Counters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Right Element: Button with sort and info (i)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentFolder != null) {
                            IconButton(
                                onClick = { historyViewTarget = Pair(null, currentFolder) },
                                modifier = Modifier.testTag("folder_history_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = "Folder History Logs",
                                    tint = activeColor,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { showSortDialog = true },
                                modifier = Modifier.testTag("sort_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort Counters",
                                    tint = activeColor,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Box {
                            IconButton(
                                onClick = { showInfoDropdown = true },
                                modifier = Modifier.testTag("info_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "View Folder Statistics",
                                    tint = activeColor,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                        // Right Dropdown Menu: Folder Stats
                        DropdownMenu(
                            expanded = showInfoDropdown,
                            onDismissRequest = { showInfoDropdown = false },
                            modifier = Modifier
                                .width(220.dp)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Folder Info",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = activeColor
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Counters", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$totalCountersCount active", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Accumulated", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$totalAccumulatedClicks", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }

                                Text(
                                    text = "Log values, manage folders, and trace board game or task progress.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    lineHeight = 14.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            }

            // Main Contents (Only counters in this active folder)
            if (counters.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(activeColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pin,
                                contentDescription = null,
                                tint = activeColor,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No counters found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentFolder != null) {
                                "Add a new counter to this folder to get started."
                            } else {
                                "Create customizable counters with step goals, colors, and notes."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.widthIn(max = 260.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showAddCounterDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = activeColor)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Counter")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(displayedCounters, key = { it.id }) { counter ->
                        CounterCard(
                            counter = counter,
                            folders = folders,
                            onIncrement = { viewModel.increment(counter) },
                            onDecrement = { viewModel.decrement(counter) },
                            onIncrementLongClick = { qolAdjustCounterData = Pair(counter, true) },
                            onDecrementLongClick = { qolAdjustCounterData = Pair(counter, false) },
                            onReset = { viewModel.reset(counter) },
                            onEdit = { counterToEdit = counter },
                            onShowLogs = { historyViewTarget = Pair(counter, null) },
                            onDuplicate = { viewModel.duplicateCounter(counter) },
                            onQuickAdjust = { amount -> viewModel.increment(counter, amount) },
                            showFolderLabel = (selectedFolderId == null)
                        )
                    }
                }
            }
        }

        // Floating Action Button to create a new counter in the bottom right corner (Circular, compact)
        FloatingActionButton(
            onClick = { showAddCounterDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(54.dp)
                .testTag("add_counter_fab"),
            containerColor = activeColor,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Counter",
                modifier = Modifier.size(24.dp)
            )
        }
    }
    } // Closing of else block for full-screen interceptor

    // Modal Dialogs
    if (showAddFolderDialog) {
        AddEditFolderDialog(
            folder = null,
            onDismiss = { showAddFolderDialog = false },
            onSave = { name, colorHex, iconName, isSmart, step, reset, target, quickButtons, thresh ->
                viewModel.createFolder(name, colorHex, iconName, isSmart, step, reset, target, quickButtons, thresh)
                showAddFolderDialog = false
            }
        )
    }

    folderToEdit?.let { folder ->
        AddEditFolderDialog(
            folder = folder,
            onDismiss = { folderToEdit = null },
            onSave = { name, colorHex, iconName, isSmart, step, reset, target, quickButtons, thresh ->
                viewModel.updateFolder(
                    folder.copy(
                        name = name,
                        colorHex = colorHex,
                        iconName = iconName,
                        isSmart = isSmart,
                        defaultStepSize = step,
                        defaultResetValue = reset,
                        defaultTargetValue = target,
                        defaultQuickButtons = quickButtons,
                        historyDividerThreshold = thresh
                    )
                )
                folderToEdit = null
            },
            onDelete = {
                viewModel.deleteFolder(folder)
                folderToEdit = null
            }
        )
    }

    if (showSortDialog) {
        SortDialog(
            initialParam = selectedSortParam,
            initialReverse = isReverseSort,
            initialAutosort = isAutosortEnabled,
            initialInterval = autosortIntervalSeconds,
            onDismiss = { showSortDialog = false },
            onApply = { param, reverse, autosort, interval ->
                selectedSortParam = param
                isReverseSort = reverse
                isAutosortEnabled = autosort
                autosortIntervalSeconds = interval
                performSort(param, reverse)
                showSortDialog = false
            }
        )
    }

    counterForLogs?.let { counter ->
        CounterLogsSheetDialog(
            counter = counter,
            viewModel = viewModel,
            onDismiss = { counterForLogs = null }
        )
    }

    qolAdjustCounterData?.let { (counter, isAddMode) ->
        QolAdjustDialog(
            counter = counter,
            initialIsAdd = isAddMode,
            onDismiss = { qolAdjustCounterData = null },
            onSave = { points, isAdd ->
                if (isAdd) {
                    viewModel.increment(counter, points)
                } else {
                    viewModel.decrement(counter, points)
                }
                qolAdjustCounterData = null
            }
        )
    }
}

@Composable
fun CounterCard(
    counter: Counter,
    folders: List<Folder>,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onIncrementLongClick: () -> Unit,
    onDecrementLongClick: () -> Unit,
    onReset: () -> Unit,
    onEdit: () -> Unit,
    onShowLogs: () -> Unit,
    onDuplicate: () -> Unit,
    onQuickAdjust: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showFolderLabel: Boolean = true
) {
    val counterColor = getColorFromHex(counter.colorHex)
    val folderName = folders.find { it.id == counter.folderId }?.name ?: "General"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, counterColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header Row: Counter Name & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Counter name with indicator circle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(counterColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = counter.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Right side: Reset, History, Settings
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier.size(36.dp).testTag("reset_button_${counter.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Counter",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onShowLogs,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "View History Logs",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate Counter",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Counter Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Compact Folder & Description row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            ) {
                if (showFolderLabel) {
                    Text(
                        text = folderName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = counterColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (counter.note.isNotEmpty()) {
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
                if (counter.note.isNotEmpty()) {
                    Text(
                        text = counter.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Counter Value & Tactile Adjustments Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrement Button (Tactile Hold Button!)
                TactileHoldButton(
                    onClick = onDecrement,
                    onLongClick = onDecrementLongClick,
                    containerColor = counterColor.copy(alpha = 0.12f),
                    contentColor = counterColor,
                    icon = Icons.Default.Remove,
                    contentDescription = "Subtract ${counter.stepSize}",
                    testTag = "decrement_button_${counter.id}"
                )

                // Count Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${counter.currentValue}",
                        style = MaterialTheme.typography.displayMedium.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }

                // Increment Button (Tactile Hold Button!)
                TactileHoldButton(
                    onClick = onIncrement,
                    onLongClick = onIncrementLongClick,
                    containerColor = counterColor,
                    contentColor = Color.White,
                    icon = Icons.Default.Add,
                    contentDescription = "Add ${counter.stepSize}",
                    testTag = "increment_button_${counter.id}"
                )
            }

            // Quick Action Buttons Row (divided by space, e.g. "-10 -5 +5 +10")
            val quickButtonsList = remember(counter.quickButtons) {
                counter.quickButtons.split("\\s+".toRegex())
                    .mapNotNull { it.trim().toIntOrNull() }
            }

            if (quickButtonsList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                val isScrollable = quickButtonsList.size > 4
                val rowModifier = if (isScrollable) {
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                } else {
                    Modifier.fillMaxWidth()
                }

                Row(
                    modifier = rowModifier,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    quickButtonsList.forEach { value ->
                        val isMinus = value < 0
                        val label = if (value > 0) "+$value" else "$value"
                        val buttonColor = counterColor
                        val textColor = Color.White

                        val itemModifier = if (isScrollable) {
                            Modifier
                                .height(36.dp)
                                .widthIn(min = 64.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(buttonColor)
                                .clickable { onQuickAdjust(value) }
                        } else {
                            Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(buttonColor)
                                .clickable { onQuickAdjust(value) }
                        }

                        Box(
                            modifier = itemModifier,
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                modifier = if (isScrollable) Modifier.padding(horizontal = 12.dp) else Modifier
                            )
                        }
                    }
                }
            }

            // Target Value Progress bar
            if (counter.targetValue != null) {
                Spacer(modifier = Modifier.height(12.dp))
                val progress = if (counter.targetValue > 0) {
                    (counter.currentValue.toFloat() / counter.targetValue.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Goal Progress: ${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${counter.currentValue} / ${counter.targetValue}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = counterColor
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = counterColor,
                        trackColor = counterColor.copy(alpha = 0.15f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TactileHoldButton(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp),
            tint = contentColor
        )
    }
}

@Composable
fun QolAdjustDialog(
    counter: Counter,
    initialIsAdd: Boolean,
    onDismiss: () -> Unit,
    onSave: (points: Int, isAdd: Boolean) -> Unit
) {
    var isAddSelected by remember { mutableStateOf(initialIsAdd) }
    var pointsText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val counterColor = getColorFromHex(counter.colorHex)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, counterColor.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Quick Adjust: ${counter.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Direction Toggle Buttons (Swapped!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // SUBTRACT (-) Toggle
                    val subBg = if (!isAddSelected) counterColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    val subContent = if (!isAddSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(subBg)
                            .clickable { isAddSelected = false }
                            .testTag("adjust_sub_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = null,
                                tint = subContent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Decrease",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = subContent
                            )
                        }
                    }

                    // ADD (+) Toggle
                    val addBg = if (isAddSelected) counterColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    val addContent = if (isAddSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(addBg)
                            .clickable { isAddSelected = true }
                            .testTag("adjust_add_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = addContent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Add",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = addContent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Numeric Input Field
                OutlinedTextField(
                    value = pointsText,
                    onValueChange = {
                        pointsText = it
                        if (it.isEmpty()) {
                            isError = false
                        } else {
                            val intVal = it.toIntOrNull()
                            isError = intVal == null || intVal <= 0
                        }
                    },
                    label = { Text("How many points?") },
                    placeholder = { Text(counter.stepSize.toString()) },
                    isError = isError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("adjust_points_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (isError) {
                    Text(
                        text = "Please enter a valid positive number",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cancel / Apply buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val pts = if (pointsText.isEmpty()) {
                                counter.stepSize
                            } else {
                                pointsText.toIntOrNull()
                            }
                            if (pts != null && pts > 0) {
                                onSave(pts, isAddSelected)
                            } else {
                                isError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = counterColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("adjust_apply_button")
                    ) {
                        Text("Apply", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomColorPickerDialog(
    initialColorHex: String,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    var hexInput by remember { mutableStateOf(initialColorHex.removePrefix("#")) }
    
    // Parse initial color
    val parsedColor = try {
        AndroidColor.parseColor("#$hexInput")
    } catch (e: Exception) {
        AndroidColor.BLUE
    }
    
    var r by remember { mutableStateOf(AndroidColor.red(parsedColor).toFloat()) }
    var g by remember { mutableStateOf(AndroidColor.green(parsedColor).toFloat()) }
    var b by remember { mutableStateOf(AndroidColor.blue(parsedColor).toFloat()) }
    
    // Derived selected color
    val selectedColor = Color(r.toInt(), g.toInt(), b.toInt())
    val selectedHex = String.format("#%02X%02X%02X", r.toInt(), g.toInt(), b.toInt())
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(0.9f).shadow(8.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Custom Color Picker",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Color Preview
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(selectedColor)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = selectedHex,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Red Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Red", style = MaterialTheme.typography.bodySmall, color = Color.Red, fontWeight = FontWeight.Bold)
                        Text("${r.toInt()}", style = MaterialTheme.typography.bodySmall)
                    }
                    Slider(
                        value = r,
                        onValueChange = { r = it },
                        valueRange = 0f..255f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Red,
                            activeTrackColor = Color.Red.copy(alpha = 0.5f)
                        )
                    )
                }
                
                // Green Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Green", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        Text("${g.toInt()}", style = MaterialTheme.typography.bodySmall)
                    }
                    Slider(
                        value = g,
                        onValueChange = { g = it },
                        valueRange = 0f..255f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF2E7D32),
                            activeTrackColor = Color(0xFF2E7D32).copy(alpha = 0.5f)
                        )
                    )
                }
                
                // Blue Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Blue", style = MaterialTheme.typography.bodySmall, color = Color.Blue, fontWeight = FontWeight.Bold)
                        Text("${b.toInt()}", style = MaterialTheme.typography.bodySmall)
                    }
                    Slider(
                        value = b,
                        onValueChange = { b = it },
                        valueRange = 0f..255f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Blue,
                            activeTrackColor = Color.Blue.copy(alpha = 0.5f)
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onColorSelected(selectedHex) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Select")
                    }
                }
            }
        }
    }
}

@Composable
fun PaletteColorsDialog(
    initialColorHex: String,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    var showCustomPicker by remember { mutableStateOf(false) }

    if (showCustomPicker) {
        CustomColorPickerDialog(
            initialColorHex = initialColorHex,
            onDismiss = { showCustomPicker = false },
            onColorSelected = {
                onColorSelected(it)
                showCustomPicker = false
            }
        )
    } else {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(0.95f).shadow(8.dp, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Material 3 Palette Colors",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(modifier = Modifier.height(280.dp)) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ExtendedColorPresets.forEach { (hex, name) ->
                                val color = getColorFromHex(hex)
                                val isSelected = initialColorHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            onColorSelected(hex)
                                        }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showCustomPicker = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = "Custom Color Picker",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Custom Picker")
                        }

                        TextButton(
                            onClick = onDismiss
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditFolderDialog(
    folder: Folder?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        colorHex: String,
        iconName: String,
        isSmart: Boolean,
        defaultStepSize: Int,
        defaultResetValue: Int,
        defaultTargetValue: Int?,
        defaultQuickButtons: String,
        historyDividerThreshold: Float
    ) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(folder?.name ?: "") }
    var selectedColorHex by remember { mutableStateOf(folder?.colorHex ?: ColorPresets[0].first) }
    var selectedIconName by remember { mutableStateOf(folder?.iconName ?: IconPresets[0].first) }

    var isSmart by remember { mutableStateOf(folder?.isSmart ?: false) }
    var defaultStepSizeStr by remember { mutableStateOf(folder?.defaultStepSize?.toString() ?: "1") }
    var defaultResetValueStr by remember { mutableStateOf(folder?.defaultResetValue?.toString() ?: "0") }
    var defaultTargetValueStr by remember { mutableStateOf(folder?.defaultTargetValue?.toString() ?: "") }
    var defaultQuickButtonsStr by remember { mutableStateOf(folder?.defaultQuickButtons ?: "") }
    var historyDividerThresholdStr by remember { mutableStateOf(folder?.historyDividerThreshold?.toString() ?: "0") }

    var errorName by remember { mutableStateOf(false) }
    var errorStepSize by remember { mutableStateOf(false) }
    var showPaletteDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 580.dp)
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (folder == null) "Create Folder" else "Folder Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorName = it.trim().isEmpty()
                    },
                    label = { Text("Folder Name") },
                    isError = errorName,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("folder_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )
                if (errorName) {
                    Text(
                        text = "Folder name cannot be empty",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Accent Color Selector
                Text(
                    text = "Select Accent Color",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Palette button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .clickable { showPaletteDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Palette Colors",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 2. Custom color indicator (if active and not in defaults)
                    val isInPresets = ColorPresets.any { it.first.equals(selectedColorHex, ignoreCase = true) }
                    if (!isInPresets) {
                        val customColor = getColorFromHex(selectedColorHex)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(customColor)
                                .border(
                                    width = 3.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                        )
                    }

                    // 3. Default presets list
                    ColorPresets.forEach { (hex, name) ->
                        val color = getColorFromHex(hex)
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Icon Selector
                Text(
                    text = "Select Folder Icon",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconPresets.forEach { (name, icon) ->
                        val isSelected = selectedIconName == name
                        val containerBg = if (isSelected) {
                            getColorFromHex(selectedColorHex)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                        val tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(containerBg)
                                .combinedClickable { selectedIconName = name },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = name,
                                tint = tint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Smart Folder Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Smart Folder Settings",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Auto-fill counter values & bulk edit matching settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isSmart,
                        onCheckedChange = { isSmart = it }
                    )
                }

                if (isSmart) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = defaultStepSizeStr,
                            onValueChange = {
                                defaultStepSizeStr = it
                                val parsed = it.toIntOrNull()
                                errorStepSize = parsed == null || parsed <= 0
                            },
                            label = { Text("Default Step") },
                            isError = errorStepSize,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = defaultResetValueStr,
                            onValueChange = { defaultResetValueStr = it },
                            label = { Text("Default Reset To") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    if (errorStepSize) {
                        Text(
                            text = "Step size must be a positive number",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = defaultTargetValueStr,
                        onValueChange = { defaultTargetValueStr = it },
                        label = { Text("Default Goal Target (Optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = defaultQuickButtonsStr,
                        onValueChange = { newValue ->
                            if (newValue.all { it in "0123456789+- " }) {
                                defaultQuickButtonsStr = newValue
                            }
                        },
                        label = { Text("Default Quick Buttons") },
                        placeholder = { Text("e.g. -10 -5 +5 +10") },
                        supportingText = {
                            Text("Only numbers and spaces are allowed.")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = historyDividerThresholdStr,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it in "0123456789." }) {
                                historyDividerThresholdStr = newValue
                            }
                        },
                        label = { Text("History Divider Interval (seconds)") },
                        placeholder = { Text("e.g. 1.5 or 10") },
                        supportingText = {
                            Text("Thin divider added to history if inactive for X seconds. 0 to disable.")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (folder != null && onDelete != null) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DELETE")
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.trim().isEmpty()) {
                                    errorName = true
                                } else {
                                    val stepSize = defaultStepSizeStr.toIntOrNull() ?: 1
                                    val resetVal = defaultResetValueStr.toIntOrNull() ?: 0
                                    val targetVal = defaultTargetValueStr.toIntOrNull()
                                    val thresh = historyDividerThresholdStr.toFloatOrNull() ?: 0f
                                    onSave(
                                        name.trim(),
                                        selectedColorHex,
                                        selectedIconName,
                                        isSmart,
                                        stepSize,
                                        resetVal,
                                        targetVal,
                                        defaultQuickButtonsStr.trim(),
                                        thresh
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    if (showPaletteDialog) {
        PaletteColorsDialog(
            initialColorHex = selectedColorHex,
            onDismiss = { showPaletteDialog = false },
            onColorSelected = {
                selectedColorHex = it
                showPaletteDialog = false
            }
        )
    }
}

@Composable
fun SortDialog(
    initialParam: String?,
    initialReverse: Boolean,
    initialAutosort: Boolean,
    initialInterval: Int,
    onDismiss: () -> Unit,
    onApply: (param: String, reverse: Boolean, autosort: Boolean, interval: Int) -> Unit
) {
    var selectedParam by remember { mutableStateOf(initialParam ?: "quantity") }
    var reverseSort by remember { mutableStateOf(initialReverse) }
    var autosortEnabled by remember { mutableStateOf(initialAutosort) }
    var autosortIntervalStr by remember { mutableStateOf(initialInterval.toString()) }

    var errorInterval by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Sort Counters",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Param selection
                Text(
                    text = "Sort By",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                val options = listOf(
                    "quantity" to "Quantity / Current Value",
                    "createdAt" to "Date of Creation",
                    "lastModified" to "Date of Change",
                    "name" to "Name"
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    options.forEach { (param, label) ->
                        val isSelected = selectedParam == param
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedParam = param }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedParam = param }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reverse sorting checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { reverseSort = !reverseSort }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = reverseSort,
                        onCheckedChange = { reverseSort = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Reverse Sorting",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (reverseSort) "From least to greatest" else "From greatest to least (Default)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Autosort checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { autosortEnabled = !autosortEnabled }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = autosortEnabled,
                        onCheckedChange = { autosortEnabled = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Auto-sort",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Keep counters sorted automatically over time",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (autosortEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = autosortIntervalStr,
                        onValueChange = {
                            autosortIntervalStr = it
                            val parsed = it.toIntOrNull()
                            errorInterval = parsed == null || parsed <= 0
                        },
                        label = { Text("Auto-sort interval (seconds)") },
                        isError = errorInterval,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (errorInterval) {
                        Text(
                            text = "Interval must be a positive number",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val interval = autosortIntervalStr.toIntOrNull() ?: 5
                            if (!autosortEnabled || interval > 0) {
                                onApply(selectedParam, reverseSort, autosortEnabled, interval)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        enabled = !autosortEnabled || (!errorInterval && autosortIntervalStr.isNotEmpty())
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCounterScreen(
    counter: Counter?,
    folders: List<Folder>,
    preselectedFolderId: Long?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        folderId: Long?,
        initialValue: Int,
        stepSize: Int,
        targetValue: Int?,
        resetValue: Int,
        colorHex: String,
        note: String,
        quickButtons: String,
        historyDividerThreshold: Float
    ) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(counter?.name ?: "") }
    var folderId by remember { mutableStateOf(counter?.folderId ?: preselectedFolderId) }
    var initialValueStr by remember { mutableStateOf(counter?.initialValue?.toString() ?: "0") }
    var currentValueStr by remember { mutableStateOf(counter?.currentValue?.toString() ?: "0") }
    var stepSizeStr by remember { mutableStateOf(counter?.stepSize?.toString() ?: "1") }
    var targetValueStr by remember { mutableStateOf(counter?.targetValue?.toString() ?: "") }
    var resetValueStr by remember { mutableStateOf(counter?.resetValue?.toString() ?: "0") }
    var selectedColorHex by remember { mutableStateOf(counter?.colorHex ?: ColorPresets[0].first) }
    var note by remember { mutableStateOf(counter?.note ?: "") }
    var quickButtonsStr by remember { mutableStateOf(counter?.quickButtons ?: "") }
    var historyDividerThresholdStr by remember { mutableStateOf(counter?.historyDividerThreshold?.toString() ?: "0") }

    var folderDropdownExpanded by remember { mutableStateOf(false) }

    var errorName by remember { mutableStateOf(false) }
    var errorStepSize by remember { mutableStateOf(false) }

    var showPaletteDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(folderId) {
        if (counter == null) {
            val selectedFolder = folders.find { it.id == folderId }
            if (selectedFolder != null && selectedFolder.isSmart) {
                stepSizeStr = selectedFolder.defaultStepSize.toString()
                resetValueStr = selectedFolder.defaultResetValue.toString()
                targetValueStr = selectedFolder.defaultTargetValue?.toString() ?: ""
                quickButtonsStr = selectedFolder.defaultQuickButtons
                historyDividerThresholdStr = selectedFolder.historyDividerThreshold.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (counter == null) "New Counter" else "Counter Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (counter != null && onDelete != null) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete Counter",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorName = it.trim().isEmpty()
                },
                label = { Text("Counter Title") },
                isError = errorName,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("counter_title_input"),
                shape = RoundedCornerShape(12.dp)
            )
            if (errorName) {
                Text(
                    text = "Title is required",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Folder selection
            ExposedDropdownMenuBox(
                expanded = folderDropdownExpanded,
                onExpandedChange = { folderDropdownExpanded = !folderDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                val currentFolder = folders.find { it.id == folderId }
                OutlinedTextField(
                    value = currentFolder?.name ?: "General / Uncategorized",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Folder Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = folderDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = folderDropdownExpanded,
                    onDismissRequest = { folderDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("General / Uncategorized") },
                        onClick = {
                            folderId = null
                            folderDropdownExpanded = false
                        }
                    )
                    folders.forEach { folder ->
                        DropdownMenuItem(
                            text = { Text(folder.name) },
                            onClick = {
                                folderId = folder.id
                                folderDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step size and Reset values Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = stepSizeStr,
                    onValueChange = {
                        stepSizeStr = it
                        val parsed = it.toIntOrNull()
                        errorStepSize = parsed == null || parsed <= 0
                    },
                    label = { Text("Step Size") },
                    isError = errorStepSize,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("step_size_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = resetValueStr,
                    onValueChange = { resetValueStr = it },
                    label = { Text("Reset To") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            if (errorStepSize) {
                Text(
                    text = "Step size must be a positive number",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Starting value and target goal limits row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (counter == null) {
                    OutlinedTextField(
                        value = initialValueStr,
                        onValueChange = { initialValueStr = it },
                        label = { Text("Initial Value") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    OutlinedTextField(
                        value = currentValueStr,
                        onValueChange = { currentValueStr = it },
                        label = { Text("Current Count") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = targetValueStr,
                    onValueChange = { targetValueStr = it },
                    label = { Text("Goal Target (Optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Short Note/Description
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Add Description / Note (Optional)") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Buttons Config Field
            OutlinedTextField(
                value = quickButtonsStr,
                onValueChange = { newValue ->
                    if (newValue.all { it in "0123456789+- " }) {
                        quickButtonsStr = newValue
                    }
                },
                label = { Text("Quick Buttons") },
                placeholder = { Text("-10 -5 +5 +10") },
                supportingText = {
                    Text("Enter numbers separated by spaces. Prefix with - to subtract.")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // History Divider Interval
            OutlinedTextField(
                value = historyDividerThresholdStr,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { it in "0123456789." }) {
                        historyDividerThresholdStr = newValue
                    }
                },
                label = { Text("History Divider Interval (seconds)") },
                placeholder = { Text("e.g. 1.5 or 10") },
                supportingText = {
                    Text("Thin divider added to history if inactive for X seconds. Set 0 to disable.")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Visual Preset Color picker
            Text(
                text = "Select Counter Card Color",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Palette button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .clickable { showPaletteDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Palette Colors",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // 3. Custom color indicator (if active and not in defaults)
                val isInPresets = ColorPresets.any { it.first.equals(selectedColorHex, ignoreCase = true) }
                if (!isInPresets) {
                    val customColor = getColorFromHex(selectedColorHex)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(customColor)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            )
                    )
                }

                // 4. Default presets list
                ColorPresets.forEach { (hex, name) ->
                    val color = getColorFromHex(hex)
                    val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColorHex = hex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = {
                        val parsedStep = stepSizeStr.toIntOrNull() ?: 1
                        if (name.trim().isEmpty() || parsedStep <= 0) {
                            if (name.trim().isEmpty()) errorName = true
                            if (parsedStep <= 0) errorStepSize = true
                        } else {
                            val initVal = initialValueStr.toIntOrNull() ?: 0
                            val currentVal = currentValueStr.toIntOrNull() ?: initVal
                            val target = targetValueStr.toIntOrNull()
                            val reset = resetValueStr.toIntOrNull() ?: 0
                            val thresh = historyDividerThresholdStr.toFloatOrNull() ?: 0f
                            onSave(
                                name.trim(),
                                folderId,
                                if (counter == null) initVal else currentVal,
                                parsedStep,
                                target,
                                reset,
                                selectedColorHex,
                                note.trim(),
                                quickButtonsStr.trim(),
                                thresh
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = getColorFromHex(selectedColorHex))
                ) {
                    Text("Save", color = Color.White)
                }
            }
        }
    }

    if (showPaletteDialog) {
        PaletteColorsDialog(
            initialColorHex = selectedColorHex,
            onDismiss = { showPaletteDialog = false },
            onColorSelected = {
                selectedColorHex = it
                showPaletteDialog = false
            }
        )
    }

    if (showDeleteConfirmation && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Counter") },
            text = { Text("Are you sure you want to permanently delete \"$name\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CounterLogsSheetDialog(
    counter: Counter,
    viewModel: CounterViewModel,
    onDismiss: () -> Unit
) {
    val logs by viewModel.getLogsForCounter(counter.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val counterColor = getColorFromHex(counter.colorHex)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.75f)
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = counter.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Timeline & Activity Log",
                            style = MaterialTheme.typography.labelMedium,
                            color = counterColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(counterColor.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Current: ${counter.currentValue}",
                            style = MaterialTheme.typography.labelMedium,
                            color = counterColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                Spacer(modifier = Modifier.height(12.dp))

                // Log History List
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No log changes registered yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(logs, key = { it.id }) { log ->
                            LogItem(log = log, counterColor = counterColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Close Timeline", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LogItem(
    log: CounterLog,
    counterColor: Color,
    modifier: Modifier = Modifier
) {
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    val dateString = formatter.format(Date(log.timestamp))

    val isPositive = log.changeValue >= 0
    val badgeColor = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
    val badgeBg = if (isPositive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.note.ifEmpty { if (isPositive) "Added count" else "Subtracted count" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Path: ${log.previousValue} ➔ ${log.newValue}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (log.changeValue >= 0) "+${log.changeValue}" else "${log.changeValue}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }
        }
    }
}

@Composable
fun FullScreenHistoryView(
    targetCounter: Counter?,
    targetFolder: Folder?,
    viewModel: CounterViewModel,
    folders: List<Folder>,
    allCounters: List<Counter>,
    onDismiss: () -> Unit
) {
    // Collect logs
    val logsState = if (targetCounter != null) {
        viewModel.getLogsForCounter(targetCounter.id).collectAsState(initial = emptyList())
    } else if (targetFolder != null) {
        val folderCounters = allCounters.filter { it.folderId == targetFolder.id }
        val ids = folderCounters.map { it.id }
        viewModel.getLogsForCounters(ids).collectAsState(initial = emptyList())
    } else {
        viewModel.getLogsForCounters(allCounters.map { it.id }).collectAsState(initial = emptyList())
    }

    val logs = logsState.value

    // Format timestamps beautifully
    val dateFormat = remember { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            targetCounter != null -> "History: ${targetCounter.name}"
                            targetFolder != null -> "Folder History: ${targetFolder.name}"
                            else -> "All History Logs"
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    
                    // 1. Local Divider Button (Active if targetCounter/targetFolder exists or at least one counter exists)
                    val canAddLocal = targetCounter != null || targetFolder != null || allCounters.isNotEmpty()
                    if (canAddLocal) {
                        IconButton(
                            onClick = {
                                if (targetCounter != null) {
                                    viewModel.insertManualDivider(targetCounter.id, isGlobal = false)
                                    android.widget.Toast.makeText(
                                        context,
                                        "Local divider added for ${targetCounter.name}!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else if (targetFolder != null) {
                                    val folderCounters = allCounters.filter { it.folderId == targetFolder.id }
                                    if (folderCounters.isNotEmpty()) {
                                        folderCounters.forEach { counter ->
                                            viewModel.insertManualDivider(counter.id, isGlobal = false)
                                        }
                                        android.widget.Toast.makeText(
                                            context,
                                            "Local divider added for counters in ${targetFolder.name}!",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        android.widget.Toast.makeText(
                                            context,
                                            "No counters in this folder to add divider to!",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    val firstCounter = allCounters.firstOrNull()
                                    if (firstCounter != null) {
                                        viewModel.insertManualDivider(firstCounter.id, isGlobal = false)
                                        android.widget.Toast.makeText(
                                            context,
                                            "Local divider added for ${firstCounter.name}!",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            modifier = Modifier.testTag("add_local_divider_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Add Local Divider"
                            )
                        }
                    }

                    // 2. Global Divider Button (Active if any counters exist)
                    if (allCounters.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.insertGlobalDivider(allCounters)
                                android.widget.Toast.makeText(
                                    context,
                                    "Global divider added to all counters & folder histories!",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.testTag("add_global_divider_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Add Global Divider"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No history records found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Display as a beautiful alternating table with left stripes and conditional history dividers
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                itemsIndexed(logs) { index, log ->
                    val counter = allCounters.find { it.id == log.counterId }
                    val counterColor = counter?.let { getColorFromHex(it.colorHex) } ?: MaterialTheme.colorScheme.primary
                    val counterName = counter?.name ?: "Unknown"

                    // Alternating background colors
                    val rowBgColor = if (index % 2 == 0) Color(0xFF0F0F0F) else Color(0xFF1F1F1F)

                    val isManualDividerLocal = log.note == "[MANUAL_DIVIDER_LOCAL]"
                    val isManualDividerGlobal = log.note == "[MANUAL_DIVIDER_GLOBAL]"

                    Column {
                        if (isManualDividerLocal || isManualDividerGlobal) {
                            val dividerLabel = if (isManualDividerLocal) "Manual Divider" else "Global Manual Divider"
                            val stripeColor = if (isManualDividerLocal) {
                                counterColor
                            } else {
                                MaterialTheme.colorScheme.secondary
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowBgColor)
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp)
                                        .background(stripeColor.copy(alpha = 0.4f))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dividerLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = stripeColor
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = try {
                                            dateFormat.format(java.util.Date(log.timestamp))
                                        } catch (e: Exception) {
                                            ""
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.LightGray.copy(alpha = 0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp)
                                        .background(stripeColor.copy(alpha = 0.4f))
                                )
                            }
                        } else {
                            // Render the standard log row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowBgColor)
                                    .height(IntrinsicSize.Min), // perfectly match stripe height
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left thin color stripe (10dp wide matching counter color)
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(10.dp)
                                        .background(counterColor)
                                )

                                // Main Row Content
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left Side: Counter name and Date/Time
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = counterName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = try {
                                                dateFormat.format(java.util.Date(log.timestamp))
                                            } catch (e: Exception) {
                                                "Unknown date"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.LightGray
                                        )
                                        if (log.note.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = log.note,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.LightGray.copy(alpha = 0.8f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Right Side: Path / Value change
                                    Column(
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        val isPositive = log.changeValue >= 0
                                        val badgeColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFEF5350)
                                        
                                        Text(
                                            text = "Path: ${log.previousValue} ➔ ${log.newValue}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.LightGray
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isPositive) "+${log.changeValue}" else "${log.changeValue}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor
                                        )
                                    }
                                }
                            }
                        }

                        // Check if we need to insert a history divider before the next log item
                        if (index < logs.size - 1) {
                            val nextLog = logs[index + 1]
                            val timeDiffSec = java.lang.Math.abs(log.timestamp - nextLog.timestamp) / 1000f

                            // Find the appropriate threshold for the current counter
                            val threshold = counter?.let { c ->
                                if (c.historyDividerThreshold > 0f) {
                                    c.historyDividerThreshold
                                } else {
                                    folders.find { it.id == c.folderId }?.let { f ->
                                        if (f.isSmart) f.historyDividerThreshold else 0f
                                    } ?: 0f
                                }
                            } ?: 0f

                            if (threshold > 0f && timeDiffSec >= threshold) {
                                // Render a thin divider marking inactivity
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                                        .padding(vertical = 4.dp, horizontal = 16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
                                        Text(
                                            text = " Inactivity: ${String.format("%.1f", timeDiffSec)}s (Limit: ${threshold}s) ",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
