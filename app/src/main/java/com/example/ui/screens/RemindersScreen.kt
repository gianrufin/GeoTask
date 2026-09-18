package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ReminderEntity
import com.example.ui.MainUiState
import com.example.ui.ReminderFilter
import com.example.ui.components.AddEditReminderDialog
import com.example.ui.components.QuickActionBar
import com.example.ui.components.ReminderCard

@Composable
fun RemindersScreen(
    state: MainUiState,
    onFilterChange: (ReminderFilter) -> Unit,
    onRefreshLocation: () -> Unit,
    onToggleActive: (ReminderEntity) -> Unit,
    onToggleFavorite: (ReminderEntity) -> Unit,
    onSaveReminder: (ReminderEntity) -> Unit,
    onDeleteReminder: (ReminderEntity) -> Unit,
    onOneTapLog: (ReminderEntity) -> Unit,
    onTestTrigger: (ReminderEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingReminder by remember { mutableStateOf<ReminderEntity?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }

    val favoriteReminders = state.reminders.filter { it.isQuickActionFavorite }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Location Status Header
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (state.currentLocation != null) {
                                        "GPS: ${String.format("%.4f", state.currentLocation.latitude)}, ${String.format("%.4f", state.currentLocation.longitude)}"
                                    } else {
                                        "Acquiring offline GPS location..."
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (state.currentLocation != null) {
                                        "Source: ${state.currentLocation.provider} • Battery-optimized"
                                    } else "Will use last known cached location",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (state.isLocating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(
                                onClick = onRefreshLocation,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("refresh_location_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh GPS",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Repetitive Task One-Tap Bar
            item {
                QuickActionBar(
                    favoriteReminders = favoriteReminders,
                    onQuickLogReminder = onOneTapLog
                )
            }

            // Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ReminderFilter.entries) { filter ->
                        FilterChip(
                            selected = (state.selectedFilter == filter),
                            onClick = { onFilterChange(filter) },
                            label = { Text(filter.label) },
                            modifier = Modifier.testTag("filter_chip_${filter.name}")
                        )
                    }
                }
            }

            // Reminder Items
            if (state.reminders.isEmpty()) {
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No location reminders found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap '+ Add Reminder' below to set your first geo-reminder with custom weekday scheduling.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(
                    items = state.reminders,
                    key = { it.id }
                ) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        currentLocation = state.currentLocation,
                        onToggleActive = { onToggleActive(reminder) },
                        onToggleFavorite = { onToggleFavorite(reminder) },
                        onEdit = { editingReminder = reminder },
                        onDelete = { onDeleteReminder(reminder) },
                        onQuickLog = { onOneTapLog(reminder) },
                        onTestTrigger = { onTestTrigger(reminder) }
                    )
                }
            }
        }

        // Add Reminder Floating Action Button
        ExtendedFloatingActionButton(
            onClick = { isAddingNew = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Add Reminder", fontWeight = FontWeight.Bold) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_reminder_fab")
        )
    }

    // Add / Edit Dialog
    if (isAddingNew) {
        AddEditReminderDialog(
            initialReminder = null,
            currentLocation = state.currentLocation,
            onDismiss = { isAddingNew = false },
            onSave = {
                onSaveReminder(it)
                isAddingNew = false
            }
        )
    }

    editingReminder?.let { reminderToEdit ->
        AddEditReminderDialog(
            initialReminder = reminderToEdit,
            currentLocation = state.currentLocation,
            onDismiss = { editingReminder = null },
            onSave = {
                onSaveReminder(it)
                editingReminder = null
            }
        )
    }
}
