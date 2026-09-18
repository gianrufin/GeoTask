package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ReminderEntity
import com.example.location.GeoLocationData
import com.example.location.LocationPresets

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditReminderDialog(
    initialReminder: ReminderEntity? = null,
    currentLocation: GeoLocationData?,
    onDismiss: () -> Unit,
    onSave: (ReminderEntity) -> Unit
) {
    var title by remember { mutableStateOf(initialReminder?.title ?: "") }
    var description by remember { mutableStateOf(initialReminder?.description ?: "") }
    var locationLabel by remember { mutableStateOf(initialReminder?.locationLabel ?: "Office HQ") }
    var latitudeStr by remember {
        mutableStateOf(
            (initialReminder?.latitude ?: currentLocation?.latitude ?: 37.7749).toString()
        )
    }
    var longitudeStr by remember {
        mutableStateOf(
            (initialReminder?.longitude ?: currentLocation?.longitude ?: -122.4194).toString()
        )
    }
    var radiusMeters by remember {
        mutableFloatStateOf((initialReminder?.radiusMeters ?: 200).toFloat())
    }
    var triggerCondition by remember {
        mutableStateOf(initialReminder?.triggerCondition ?: "ENTER")
    }
    var weekdaysOnly by remember {
        mutableStateOf(initialReminder?.weekdaysOnly ?: true)
    }
    var selectedDaysMask by remember {
        mutableIntStateOf(initialReminder?.selectedDaysMask ?: ReminderEntity.MASK_WEEKDAYS)
    }
    var isQuickActionFavorite by remember {
        mutableStateOf(initialReminder?.isQuickActionFavorite ?: true)
    }

    var titleError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = if (initialReminder == null) "New Location Reminder" else "Edit Location Reminder",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Reminder Title *") },
                    placeholder = { Text("e.g. Clock In & Standup") },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Title is required") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminder_title_input"),
                    singleLine = true
                )

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes / Sub-tasks (Optional)") },
                    placeholder = { Text("e.g. Bring badge, check in with supervisor") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminder_description_input"),
                    maxLines = 3
                )

                // Location Presets
                Text(
                    text = "Quick Location Presets",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LocationPresets.PRESETS.forEach { preset ->
                        FilterChip(
                            selected = (locationLabel == preset.name),
                            onClick = {
                                locationLabel = preset.name
                                latitudeStr = preset.latitude.toString()
                                longitudeStr = preset.longitude.toString()
                            },
                            label = { Text(preset.name) }
                        )
                    }
                }

                // Location Label
                OutlinedTextField(
                    value = locationLabel,
                    onValueChange = { locationLabel = it },
                    label = { Text("Location Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Coordinates Row & Current GPS Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = latitudeStr,
                        onValueChange = { latitudeStr = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = longitudeStr,
                        onValueChange = { longitudeStr = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    if (currentLocation != null) {
                        IconButton(
                            onClick = {
                                latitudeStr = currentLocation.latitude.toString()
                                longitudeStr = currentLocation.longitude.toString()
                            },
                            modifier = Modifier.testTag("use_gps_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Use My GPS Location",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Radius Setting
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Geofence Radius",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${radiusMeters.toInt()} meters",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = radiusMeters,
                        onValueChange = { radiusMeters = it },
                        valueRange = 50f..1000f,
                        steps = 18,
                        modifier = Modifier.testTag("radius_slider")
                    )
                }

                // Trigger Condition
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ENTER" to "On Enter", "NEARBY" to "Nearby", "EXIT" to "On Exit").forEach { (key, name) ->
                        FilterChip(
                            selected = (triggerCondition == key),
                            onClick = { triggerCondition = key },
                            label = { Text(name) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Weekday Schedule Section
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Weekdays Only (Mon - Fri)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Mutes alerts on Saturday & Sunday",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = weekdaysOnly,
                                onCheckedChange = { isChecked ->
                                    weekdaysOnly = isChecked
                                    selectedDaysMask = if (isChecked) ReminderEntity.MASK_WEEKDAYS else ReminderEntity.MASK_ALL
                                },
                                modifier = Modifier.testTag("weekdays_only_switch")
                            )
                        }

                        // Day of week selector chips
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Active Days:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ReminderEntity.DAY_NAMES.forEachIndexed { index, dayName ->
                                val isSelected = ReminderEntity.isDaySelected(selectedDaysMask, index)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        val newMask = ReminderEntity.toggleDay(selectedDaysMask, index)
                                        selectedDaysMask = newMask
                                        weekdaysOnly = (newMask == ReminderEntity.MASK_WEEKDAYS)
                                    },
                                    label = { Text(dayName, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                // Favorite for One-Tap Quick Log
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isQuickActionFavorite,
                        onCheckedChange = { isQuickActionFavorite = it },
                        modifier = Modifier.testTag("quick_action_favorite_checkbox")
                    )
                    Text(
                        text = "Pin to One-Tap Quick Action bar",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    val lat = latitudeStr.toDoubleOrNull() ?: 37.7749
                    val lon = longitudeStr.toDoubleOrNull() ?: -122.4194

                    val entity = (initialReminder ?: ReminderEntity(
                        title = title.trim(),
                        locationLabel = locationLabel.ifBlank { "Location" },
                        latitude = lat,
                        longitude = lon
                    )).copy(
                        title = title.trim(),
                        description = description.trim(),
                        locationLabel = locationLabel.ifBlank { "Location" },
                        latitude = lat,
                        longitude = lon,
                        radiusMeters = radiusMeters.toInt(),
                        triggerCondition = triggerCondition,
                        weekdaysOnly = weekdaysOnly,
                        selectedDaysMask = selectedDaysMask,
                        isQuickActionFavorite = isQuickActionFavorite
                    )
                    onSave(entity)
                },
                modifier = Modifier.testTag("dialog_save_button")
            ) {
                Text("Save Reminder")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_cancel_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
