package com.lipo.menu.presentation.pairinglist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lipo.menu.data.model.Dish

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePairingListDialog(
    availableDishes: List<Dish> = emptyList(),
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String?, dishIds: List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val selectedDishIds = remember { mutableStateListOf<String>() }
    var nameError by remember { mutableStateOf<String?>(null) }
    var dishError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "创建搭配清单",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("名称 *") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述 (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dish selection
                Text(
                    text = "选择菜品 *",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (dishError != null) {
                    Text(
                        text = dishError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (availableDishes.isEmpty()) {
                    Text(
                        text = "暂无可用菜品，请先添加菜品",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(availableDishes) { dish ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = dish.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Checkbox(
                                    checked = dish.id in selectedDishIds,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            selectedDishIds.add(dish.id)
                                        } else {
                                            selectedDishIds.remove(dish.id)
                                        }
                                        dishError = null
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }

                    Button(
                        onClick = {
                            // Validation
                            var hasError = false
                            if (name.isBlank()) {
                                nameError = "名称不能为空"
                                hasError = true
                            }
                            if (selectedDishIds.isEmpty()) {
                                dishError = "请至少选择一道菜品"
                                hasError = true
                            }

                            if (!hasError) {
                                onCreate(name, description.ifBlank { null }, selectedDishIds.toList())
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("创建")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPairingListDialog(
    initialName: String,
    initialDescription: String?,
    onDismiss: () -> Unit,
    onUpdate: (name: String, description: String?) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "编辑搭配清单",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("名称 *") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述 (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = "名称不能为空"
                            } else {
                                onUpdate(name, description.ifBlank { null })
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}

@Composable
fun DeletePairingListDialog(
    pairingListName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = "😢",
                style = MaterialTheme.typography.displaySmall
            )
        },
        title = {
            Text(
                "确认删除",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                "确定要删除「$pairingListName」吗？\n删除后将无法恢复哦～",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onConfirm,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                Text("确认删除")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDishToPairingListDialog(
    availableDishes: List<Dish>,
    onDismiss: () -> Unit,
    onAdd: (dishId: String) -> Unit
) {
    var selectedDishId by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "添加菜品",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (availableDishes.isEmpty()) {
                    Text(
                        text = "所有菜品都已添加到这个搭配清单中",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(availableDishes) { dish ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = dish.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                RadioButton(
                                    selected = dish.id == selectedDishId,
                                    onClick = { selectedDishId = dish.id }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }

                    Button(
                        onClick = {
                            selectedDishId?.let { onAdd(it) }
                        },
                        enabled = selectedDishId != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("添加")
                    }
                }
            }
        }
    }
}

@Composable
fun RemoveDishFromPairingDialog(
    dishName: String,
    isLastDish: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (isLastDish) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Text(
                    text = "⚠️",
                    style = MaterialTheme.typography.displaySmall
                )
            },
            title = {
                Text(
                    "无法移除最后一道菜",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "这是搭配清单中的最后一道菜，无法移除。\n建议直接删除整个搭配清单。",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("知道了")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Text(
                    text = "🤔",
                    style = MaterialTheme.typography.displaySmall
                )
            },
            title = {
                Text(
                    "确认移除",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "确定要将「$dishName」从这个搭配清单中移除吗？",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    )
                ) {
                    Text("确认移除")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text("取消")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
