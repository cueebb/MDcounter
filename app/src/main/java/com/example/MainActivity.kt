@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

    // Aggregate statistics
    val totalCountersCount = counters.size
    val totalAccumulatedClicks = counters.sumOf { it.currentValue }
    val currentFolder = folders.find { it.id == selectedFolderId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Counter Tracker",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Log values, manage folders, trace progress",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .combinedClickable(
                                onClick = { showAddCounterDialog = true }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Counter",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // App Quick Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Numbers,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Counters",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$totalCountersCount active",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Accumulated",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$totalAccumulatedClicks",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Folders Selection Row
        Text(
            text = "Folders / Categories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // "All" folder selection
            item {
                val isSelected = selectedFolderId == null
                val folderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                Card(
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("folder_all")
                        .combinedClickable(
                            onClick = { viewModel.selectFolder(null) }
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = folderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "All",
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Custom Folders from Database
            items(folders, key = { it.id }) { folder ->
                val isSelected = selectedFolderId == folder.id
                val folderColor = getColorFromHex(folder.colorHex)
                val isSystemDark = isSystemInDarkTheme()

                val cardBg = if (isSelected) {
                    folderColor
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }

                val contentColor = if (isSelected) {
                    Color.White
                } else {
                    MaterialTheme.colorScheme.onSurface
                }

                Card(
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("folder_${folder.id}")
                        .combinedClickable(
                            onClick = { viewModel.selectFolder(folder.id) },
                            onLongClick = { folderToEdit = folder }
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White.copy(alpha = 0.2f) else folderColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconByName(folder.iconName),
                                contentDescription = null,
                                tint = if (isSelected) Color.White else folderColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = folder.name,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Add Folder Button
            item {
                Card(
                    modifier = Modifier
                        .size(54.dp)
                        .testTag("add_folder_button")
                        .combinedClickable(
                            onClick = { showAddFolderDialog = true }
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "Create Folder",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Counters Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (currentFolder != null) "${currentFolder.name} Counters" else "All Counters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (currentFolder != null) {
                TextButton(
                    onClick = { folderToEdit = currentFolder },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Folder settings",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Folder Settings", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Empty State / Counters list
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
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pin,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
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
                            "Add a new counter to this custom folder to get started."
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
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Counter")
                    }
                }
            }
        } else {
            // Counters Vertical Scroll List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(counters, key = { it.id }) { counter ->
                    CounterCard(
                        counter = counter,
                        folders = folders,
                        onIncrement = { viewModel.increment(counter) },
                        onDecrement = { viewModel.decrement(counter) },
                        onReset = { viewModel.reset(counter) },
                        onEdit = { counterToEdit = counter },
                        onShowLogs = { counterForLogs = counter }
                    )
                }
            }
        }
    }

    // Modal Dialogs
    if (showAddFolderDialog) {
        AddEditFolderDialog(
            folder = null,
            onDismiss = { showAddFolderDialog = false },
            onSave = { name, colorHex, iconName ->
                viewModel.createFolder(name, colorHex, iconName)
                showAddFolderDialog = false
            }
        )
    }

    folderToEdit?.let { folder ->
        AddEditFolderDialog(
            folder = folder,
            onDismiss = { folderToEdit = null },
            onSave = { name, colorHex, iconName ->
                viewModel.updateFolder(folder.copy(name = name, colorHex = colorHex, iconName = iconName))
                folderToEdit = null
            },
            onDelete = {
                viewModel.deleteFolder(folder)
                folderToEdit = null
            }
        )
    }

    if (showAddCounterDialog) {
        AddEditCounterDialog(
            counter = null,
            folders = folders,
            preselectedFolderId = selectedFolderId,
            onDismiss = { showAddCounterDialog = false },
            onSave = { name, folderId, initVal, step, target, reset, color, note ->
                viewModel.createCounter(name, folderId, initVal, step, target, reset, color, note)
                showAddCounterDialog = false
            }
        )
    }

    counterToEdit?.let { counter ->
        AddEditCounterDialog(
            counter = counter,
            folders = folders,
            preselectedFolderId = counter.folderId,
            onDismiss = { counterToEdit = null },
            onSave = { name, folderId, initVal, step, target, reset, color, note ->
                viewModel.updateCounter(
                    counter.copy(
                        name = name,
                        folderId = folderId,
                        initialValue = initVal,
                        stepSize = step,
                        targetValue = target,
                        resetValue = reset,
                        colorHex = color,
                        note = note
                    )
                )
                counterToEdit = null
            },
            onDelete = {
                viewModel.deleteCounter(counter)
                counterToEdit = null
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
}

@Composable
fun CounterCard(
    counter: Counter,
    folders: List<Folder>,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
    onEdit: () -> Unit,
    onShowLogs: () -> Unit,
    modifier: Modifier = Modifier
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
            modifier = Modifier.padding(16.dp)
        ) {
            // Counter Header / Category & Edit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(counterColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = folderName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = counterColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row {
                    IconButton(
                        onClick = onShowLogs,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "View History Logs",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
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
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Counter Title
            Text(
                text = counter.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Description / Note if set
            if (counter.note.isNotEmpty()) {
                Text(
                    text = counter.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Counter Value & Tactile Adjustments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrement Button
                FilledIconButton(
                    onClick = onDecrement,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("decrement_button_${counter.id}"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = counterColor.copy(alpha = 0.12f),
                        contentColor = counterColor
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Subtract ${counter.stepSize}",
                        modifier = Modifier.size(22.dp)
                    )
                }

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

                // Increment Button
                FilledIconButton(
                    onClick = onIncrement,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("increment_button_${counter.id}"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = counterColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add ${counter.stepSize}",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar and resets
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (counter.targetValue != null) {
                        val progress = if (counter.targetValue > 0) {
                            (counter.currentValue.toFloat() / counter.targetValue.toFloat()).coerceIn(0f, 1f)
                        } else 0f

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
                    } else {
                        // Quick step-size indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Step: +${counter.stepSize}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Quick Reset Button
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("reset_button_${counter.id}"),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Counter",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RESET",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditFolderDialog(
    folder: Folder?,
    onDismiss: () -> Unit,
    onSave: (name: String, colorHex: String, iconName: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(folder?.name ?: "") }
    var selectedColorHex by remember { mutableStateOf(folder?.colorHex ?: ColorPresets[0].first) }
    var selectedIconName by remember { mutableStateOf(folder?.iconName ?: IconPresets[0].first) }

    var errorName by remember { mutableStateOf(false) }

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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ColorPresets.take(4).forEach { (hex, nameStr) ->
                        val color = getColorFromHex(hex)
                        val isSelected = selectedColorHex == hex
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
                                .combinedClickable { selectedColorHex = hex }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ColorPresets.drop(4).take(4).forEach { (hex, nameStr) ->
                        val color = getColorFromHex(hex)
                        val isSelected = selectedColorHex == hex
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
                                .combinedClickable { selectedColorHex = hex }
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
                                    onSave(name.trim(), selectedColorHex, selectedIconName)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCounterDialog(
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
        note: String
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

    var folderDropdownExpanded by remember { mutableStateOf(false) }

    var errorName by remember { mutableStateOf(false) }
    var errorStepSize by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
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
                    text = if (counter == null) "New Counter" else "Counter Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

                // Step size and Reset values Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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

                Spacer(modifier = Modifier.height(12.dp))

                // Starting value and target goal limits
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Only show Initial Value settings on counter creation to protect logs
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
                        // On editing, allow directly altering current count safely
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
                        label = { Text("Goal Target (Opt)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

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

                // Visual Preset Color picker
                Text(
                    text = "Select Counter Card Color",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ColorPresets.take(4).forEach { (hex, name) ->
                        val color = getColorFromHex(hex)
                        val isSelected = selectedColorHex == hex
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
                                .combinedClickable { selectedColorHex = hex }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ColorPresets.drop(4).take(4).forEach { (hex, nameStr) ->
                        val color = getColorFromHex(hex)
                        val isSelected = selectedColorHex == hex
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
                                .combinedClickable { selectedColorHex = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (counter != null && onDelete != null) {
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
                                val parsedStep = stepSizeStr.toIntOrNull() ?: 1
                                if (name.trim().isEmpty() || parsedStep <= 0) {
                                    if (name.trim().isEmpty()) errorName = true
                                    if (parsedStep <= 0) errorStepSize = true
                                } else {
                                    val initVal = initialValueStr.toIntOrNull() ?: 0
                                    val currentVal = currentValueStr.toIntOrNull() ?: initVal
                                    val target = targetValueStr.toIntOrNull()
                                    val reset = resetValueStr.toIntOrNull() ?: 0
                                    // Save
                                    onSave(
                                        name.trim(),
                                        folderId,
                                        if (counter == null) initVal else currentVal,
                                        parsedStep,
                                        target,
                                        reset,
                                        selectedColorHex,
                                        note.trim()
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
