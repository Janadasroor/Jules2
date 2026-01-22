package com.jnd.jules.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jnd.jules.model.Session
import com.jnd.jules.model.SessionState
import com.jnd.jules.util.PreferenceManager

import androidx.compose.material3.pulltorefresh.PullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    viewModel: SessionViewModel = viewModel(),
    onSessionClick: (Session) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val sessions by viewModel.sessions.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val sources by viewModel.sources.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    // API Key State
    var isApiKeySet by remember { mutableStateOf(true) }

    fun checkApiKey() {
        val key = PreferenceManager.getApiKey()
        isApiKeySet = !key.isNullOrBlank()
    }
    
    fun refresh() {
        isRefreshing = true
        viewModel.fetchSessions()
        viewModel.fetchSources()
    }

    LaunchedEffect(Unit) {
        checkApiKey()
        viewModel.fetchSessions()
        viewModel.fetchSources()
    }
    
    // Update refreshing state when loading changes
    LaunchedEffect(loading) {
        if (!loading) {
            isRefreshing = false
        }
    }
    
    // Re-check when coming back to this screen
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        checkApiKey()
        if (isApiKeySet) {
             viewModel.fetchSessions()
             viewModel.fetchSources()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jules Sessions") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isApiKeySet) {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Create Session")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column {
                if (!isApiKeySet) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "API Key Missing",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                "Please set your Jules API Key in Settings to continue.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onSettingsClick) {
                                Text("Go to Settings")
                            }
                        }
                    }
                }
                
                if (loading && !isRefreshing) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                         CircularProgressIndicator()
                    }
                } else if (error != null && !isRefreshing) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error: $error",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                            Button(onClick = { refresh() }) {
                                Text("Retry")
                            }
                        }
                    }
                } else {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { refresh() },
                        modifier = Modifier.weight(1f)
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(sessions) { session ->
                                SessionItem(session, onClick = { onSessionClick(session) })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateSessionDialog(
            sources = sources,
            onDismiss = { showCreateDialog = false },
            onConfirm = { prompt, source ->
                viewModel.createSession(prompt, source)
                showCreateDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionDialog(
    sources: List<com.jnd.jules.model.Source>,
    onDismiss: () -> Unit,
    onConfirm: (String, com.jnd.jules.model.Source) -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var selectedSource by remember { mutableStateOf<com.jnd.jules.model.Source?>(null) }
    var expanded by remember { mutableStateOf(false) }

    // If there's only one source, select it automatically or default if list is empty
    LaunchedEffect(sources) {
        if (sources.isNotEmpty() && selectedSource == null) {
            selectedSource = sources.first()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Session") },
        text = {
            Column {
                Text("Select Source:")
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedSource?.name ?: "Select a Source",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        sources.forEach { source ->
                            DropdownMenuItem(
                                text = { Text(source.name) },
                                onClick = {
                                    selectedSource = source
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Enter a prompt for the new session:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("Prompt") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (prompt.isNotBlank() && selectedSource != null) {
                        onConfirm(prompt, selectedSource!!)
                    }
                },
                enabled = prompt.isNotBlank() && selectedSource != null
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SessionItem(session: Session, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = session.title ?: session.name ?: "Untitled Session",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Status badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = getStatusColor(session.state),
                    contentColor = Color.White
                ) {
                    Text(
                        text = session.state?.name ?: "UNKNOWN",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = session.createTime ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!session.prompt.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = session.prompt,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }
        }
    }
}


