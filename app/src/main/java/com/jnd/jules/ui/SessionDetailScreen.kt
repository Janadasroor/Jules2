package com.jnd.jules.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jnd.jules.model.Session
import com.jnd.jules.model.SessionState

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: String,
    viewModel: SessionViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val session by viewModel.selectedSession.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    fun refresh() {
        isRefreshing = true
        viewModel.getSession(sessionId)
    }

    LaunchedEffect(sessionId) {
        viewModel.getSession(sessionId)
    }
    
    // Auto-refresh for active states
    LaunchedEffect(session?.state) {
        val activeStates = listOf(
            SessionState.QUEUED, 
            SessionState.PLANNING, 
            SessionState.IN_PROGRESS,
            SessionState.AWAITING_PLAN_APPROVAL
        )
        while (session?.state in activeStates) {
            delay(5000) // Poll every 5 seconds
            viewModel.getSession(sessionId)
        }
    }

    // Update refreshing state when loading changes
    LaunchedEffect(loading) {
        if (!loading) {
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (session != null) {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { refresh() }
                ) {
                    SessionDetailContent(
                        session = session!!,
                        activities = activities,
                        onSendMessage = { message ->
                            viewModel.sendMessage(message)
                        },
                        onApprovePlan = {
                            viewModel.approvePlan()
                        }
                    )
                }
                
                if (loading && !isRefreshing) {
                     LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
                }
            } else if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
        }
    }
}

@Composable
fun SessionDetailContent(
    session: Session,
    activities: List<com.jnd.jules.model.Activity>,
    onSendMessage: (String) -> Unit = {},
    onApprovePlan: () -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Show scroll-to-bottom button when not at bottom
    val showScrollToBottom by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem != null && lastVisibleItem.index < totalItems - 1
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Status and Title
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiveStatusBadge(session.state ?: SessionState.STATE_UNSPECIFIED)
                            
                            if (session.state == SessionState.AWAITING_PLAN_APPROVAL) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Button(onClick = onApprovePlan) {
                                    Text("Approve Plan")
                                }
                            }
                        }

                        Text(
                            text = session.title ?: session.name ?: "Untitled Session",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        // Metadata
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                LabelValue("ID", session.id ?: "N/A")
                                LabelValue("Created", session.createTime ?: "N/A")
                                LabelValue("Source", session.sourceContext.source)
                                session.sourceContext.githubRepoContext?.let {
                                    LabelValue("Branch", it.startingBranch)
                                }
                            }
                        }
                        
                        // Activities header
                        Text("Activity History", style = MaterialTheme.typography.titleMedium)
                        
                        // Initial prompt
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("User", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                ExpandableText(session.prompt)
                            }
                        }
                    }
                }
                
                // Activity items
                items(activities, key = { it.createTime ?: it.hashCode().toString() }) { activity ->
                    ActivityCard(activity)
                }
                
                // Outputs section
                if (!session.outputs.isNullOrEmpty()) {
                    item {
                        val context = LocalContext.current
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Outputs", style = MaterialTheme.typography.titleMedium)
                            session.outputs.forEach { output ->
                                output.pullRequest?.let { pr ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            pr.url?.let { url ->
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                            }
                                        }
                                    ) {
                                        Column(Modifier.padding(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Info, contentDescription = "Pull Request", tint = MaterialTheme.colorScheme.primary)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Pull Request", style = MaterialTheme.typography.labelLarge)
                                                Spacer(Modifier.weight(1f))
                                                Text("Tap to open", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(pr.title ?: "No Title", style = MaterialTheme.typography.bodyLarge)
                                            Text(
                                                pr.url ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Message Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Send a message...") },
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
        
        // Scroll to bottom FAB
        if (showScrollToBottom) {
            SmallFloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 80.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Scroll to bottom")
            }
        }
    }
}

// Expandable text for long messages
@Composable
fun ExpandableText(
    text: String,
    maxLines: Int = 8,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge
) {
    var isExpanded by remember { mutableStateOf(false) }
    var needsExpansion by remember { mutableStateOf(false) }
    
    SelectionContainer {
        Column {
            Text(
                text = text,
                style = style,
                maxLines = if (isExpanded) Int.MAX_VALUE else maxLines,
                overflow = if (isExpanded) androidx.compose.ui.text.style.TextOverflow.Visible else androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                onTextLayout = { textLayoutResult ->
                    needsExpansion = textLayoutResult.hasVisualOverflow || textLayoutResult.lineCount > maxLines
                }
            )
            if (needsExpansion || isExpanded) {
                Text(
                    text = if (isExpanded) "Show less" else "Show more",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { isExpanded = !isExpanded }
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun LabelValue(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = "$label: ", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(100.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ActivityCard(activity: com.jnd.jules.model.Activity) {
    val containerColor = when {
        activity.userMessaged != null -> MaterialTheme.colorScheme.primaryContainer
        activity.agentMessaged != null -> MaterialTheme.colorScheme.secondaryContainer
        activity.planGenerated != null -> MaterialTheme.colorScheme.tertiaryContainer
        activity.sessionFailed != null -> MaterialTheme.colorScheme.errorContainer
        activity.sessionCompleted != null -> Color(0xFFD0F0C0) // Light green
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header with originator and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = activity.originator?.replaceFirstChar { it.uppercase() } ?: "System",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                activity.createTime?.let {
                    Text(
                        text = it.take(19).replace("T", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Main content based on activity type
            when {
                activity.userMessaged != null -> {
                    SelectionContainer {
                        ExpandableText(
                            text = activity.userMessaged.userMessage ?: "",
                            maxLines = 10
                        )
                    }
                }
                activity.agentMessaged != null -> {
                    SelectionContainer {
                        ExpandableText(
                            text = activity.agentMessaged.agentMessage ?: "",
                            maxLines = 15
                        )
                    }
                }
                activity.planGenerated != null -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.List, contentDescription = "Plan", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Plan Generated",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    activity.planGenerated.plan?.steps?.forEachIndexed { index, step ->
                        Row(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(24.dp)
                            )
                            Column {
                                Text(
                                    text = step.title ?: "Step ${index + 1}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                step.description?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                activity.planApproved != null -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Approved", tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Plan Approved",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                activity.progressUpdated != null -> {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                             Icon(Icons.Default.Refresh, contentDescription = "Progress", tint = MaterialTheme.colorScheme.primary)
                             Spacer(modifier = Modifier.width(8.dp))
                             Text(
                                text = activity.progressUpdated.title ?: "Progress Update",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        activity.progressUpdated.description?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 32.dp, top = 4.dp)
                            )
                        }
                    }
                }
                activity.sessionCompleted != null -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Session Completed",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                activity.sessionFailed != null -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "Failed", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Session Failed: ${activity.sessionFailed.reason ?: "Unknown reason"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // Display artifacts if present
            activity.artifacts?.forEach { artifact ->
                Spacer(modifier = Modifier.height(12.dp))
                ArtifactContent(artifact)
            }
        }
    }
}

@Composable
fun ArtifactContent(artifact: com.jnd.jules.model.Artifact) {
    Column {
        if (artifact.changeSet != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = "Code Changes", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Code Changes",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    artifact.changeSet.source?.let {
                        Text(
                            text = "Source: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 32.dp)
                        )
                    }
                    artifact.changeSet.gitPatch?.let { patch ->
                        Spacer(modifier = Modifier.height(8.dp))
                        patch.suggestedCommitMessage?.let {
                            Text(
                                text = "Commit: $it",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 32.dp)
                            )
                        }
                        patch.unidiffPatch?.let { diff ->
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Parse and display file stats with expandable content
                            val fileDiffs = parseGitDiffByFile(diff)
                            
                            fileDiffs.forEach { fileDiff ->
                                var isFileExpanded by remember { mutableStateOf(false) }
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column {
                                        // Header
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { isFileExpanded = !isFileExpanded }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                if (isFileExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = if (isFileExpanded) "Collapse" else "Expand",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = fileDiff.fileName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                ),
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                            
                                            if (fileDiff.added > 0) {
                                                Surface(
                                                    color = Color(0xFF1E3A2A), // Dark Green
                                                    shape = RoundedCornerShape(4.dp),
                                                    modifier = Modifier.padding(start = 8.dp)
                                                ) {
                                                    Text(
                                                        text = "+${fileDiff.added}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                                        color = Color(0xFF4CAF50),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            
                                            if (fileDiff.removed > 0) {
                                                Surface(
                                                    color = Color(0xFF3E1E1E), // Dark Red
                                                    shape = RoundedCornerShape(4.dp),
                                                    modifier = Modifier.padding(start = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "-${fileDiff.removed}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                                        color = Color(0xFFF44336), 
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        
                                        // Content
                                        if (isFileExpanded) {
                                            HorizontalDivider(color = MaterialTheme.colorScheme.surface)
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                color = Color(0xFF1E1E1E)
                                            ) {
                                                SelectionContainer {
                                                    Column(Modifier.padding(8.dp).horizontalScroll(rememberScrollState())) {
                                                        fileDiff.content.lines().forEachIndexed { index, line ->
                                                            val (paramBgColor, textColor) = when {
                                                                line.startsWith("+") && !line.startsWith("+++") -> Color(0xFF1E3A2A) to Color(0xFFC3E88D) // Greenish
                                                                line.startsWith("-") && !line.startsWith("---") -> Color(0xFF3E1E1E) to Color(0xFFFF5370) // Reddish
                                                                line.startsWith("@@") -> Color.Transparent to Color(0xFF89DDFF) // Blueish for hunk header
                                                                else -> Color.Transparent to Color(0xFFD4D4D4)
                                                            }
                                                            
                                                            Row(modifier = Modifier.fillMaxWidth().background(paramBgColor)) {
                                                                Text(
                                                                    text = "${index + 1}",
                                                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                                                    color = Color.Gray,
                                                                    modifier = Modifier.width(32.dp).padding(end = 8.dp),
                                                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                                                )
                                                                Text(
                                                                    text = line,
                                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                                    ),
                                                                    color = textColor
                                                                )
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
                    }
                }
            }
        }
        
        if (artifact.bashOutput != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Terminal", tint = MaterialTheme.colorScheme.primary)
                         Spacer(modifier = Modifier.width(8.dp))
                         Text(
                            text = "Command Output",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    artifact.bashOutput.command?.let {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            color = Color(0xFF2D2D2D),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            SelectionContainer {
                                Text(
                                    text = "$ $it",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    ),
                                    color = Color(0xFF4EC9B0),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                    artifact.bashOutput.output?.let { output ->
                        var isExpanded by remember { mutableStateOf(false) }
                        val maxHeight = if (isExpanded) 400.dp else 150.dp
                        
                        Column {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .heightIn(max = maxHeight),
                                color = Color(0xFF1E1E1E),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                SelectionContainer {
                                    Text(
                                        text = output,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        ),
                                        color = Color(0xFFD4D4D4),
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .horizontalScroll(rememberScrollState())
                                            .verticalScroll(rememberScrollState())
                                    )
                                }
                            }
                            if (output.lines().size > 8) {
                                Row(
                                    modifier = Modifier
                                        .clickable { isExpanded = !isExpanded }
                                        .padding(top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Expand",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (isExpanded) "Collapse" else "Expand output",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    artifact.bashOutput.exitCode?.let { code ->
                        Text(
                            text = "Exit code: $code",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (code == 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        
        if (artifact.media != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "Media", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Media: ${artifact.media.mimeType ?: "Unknown type"}",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Text(
                        text = "Media content available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 32.dp)
                    )
                }
            }
        }
    }
}

data class FileDiffStats(val fileName: String, var added: Int = 0, var removed: Int = 0)

fun parseGitDiffStats(diff: String): List<FileDiffStats> {
    val stats = mutableListOf<FileDiffStats>()
    var currentFile: FileDiffStats? = null

    diff.lines().forEach { line ->
        when {
            line.startsWith("diff --git") -> {
                currentFile = null
            }
            line.startsWith("+++ b/") -> {
                val fileName = line.removePrefix("+++ b/")
                currentFile = FileDiffStats(fileName).also { stats.add(it) }
            }
            line.startsWith("+++ ") -> {
                val fileName = line.removePrefix("+++ ")
                if (currentFile == null) {
                    currentFile = FileDiffStats(fileName).also { stats.add(it) }
                }
            }
            line.startsWith("+") && !line.startsWith("+++") -> {
                currentFile?.let { it.added++ }
            }
            line.startsWith("-") && !line.startsWith("---") -> {
                currentFile?.let { it.removed++ }
            }
        }
    }
    return stats
}

data class FileDiff(
    val fileName: String,
    val content: String,
    var added: Int = 0,
    var removed: Int = 0
)

fun parseGitDiffByFile(diff: String): List<FileDiff> {
    val fileDiffs = mutableListOf<FileDiff>()
    var currentFileName: String? = null
    val currentContent = StringBuilder()
    var currentAdded = 0
    var currentRemoved = 0
    var insideHunk = false

    // Helper to save current file
    fun saveCurrentFile() {
        if (currentFileName != null && currentContent.isNotEmpty()) {
            fileDiffs.add(
                FileDiff(
                    fileName = currentFileName!!,
                    content = currentContent.toString().trim(),
                    added = currentAdded,
                    removed = currentRemoved
                )
            )
        }
    }

    diff.lines().forEach { line ->
        if (line.startsWith("diff --git")) {
            saveCurrentFile()
            currentFileName = null
            currentContent.clear()
            currentAdded = 0
            currentRemoved = 0
            insideHunk = false
        } else if (line.startsWith("+++ b/")) {
            currentFileName = line.removePrefix("+++ b/")
        } else if (line.startsWith("+++ ") && currentFileName == null) {
             currentFileName = line.removePrefix("+++ ")
        }
        
        // Check if we are entering a hunk
        if (line.startsWith("@@")) {
            insideHunk = true
        }

        // Only append lines if we are inside a hunk or if it's a relevant line like binary handling
        // We gracefully skip headers like index, ---, +++, diff --git
        if (insideHunk) {
            currentContent.append(line).append("\n")
            
            if (line.startsWith("+") && !line.startsWith("+++")) {
                currentAdded++
            } else if (line.startsWith("-") && !line.startsWith("---")) {
                currentRemoved++
            }
        }
    }
    // Save the last file
    saveCurrentFile()
    
    return fileDiffs
}


