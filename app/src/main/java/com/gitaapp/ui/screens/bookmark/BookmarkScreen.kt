package com.gitaapp.ui.screens.bookmark

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gitaapp.data.model.Bookmark
import com.gitaapp.ui.components.EmptyState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    onVerseClick: (Int, Int) -> Unit,
    viewModel: BookmarkViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookmarks", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is BookmarkUiState.Loading -> Unit

            is BookmarkUiState.Empty ->
                EmptyState(
                    icon = "🔖", title = "No bookmarks yet",
                    subtitle = "Tap the bookmark icon on any verse to save it here for quick access.",
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                )

            is BookmarkUiState.Success ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = state.bookmarks, key = { it.verseId }) { bookmark ->
                        SwipeToDeleteBookmark(
                            bookmark = bookmark,
                            onClick  = { onVerseClick(bookmark.chapterNumber, bookmark.verseNumber) },
                            onDelete = { viewModel.removeBookmark(bookmark.verseId) }
                        )
                    }
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteBookmark(bookmark: Bookmark, onClick: () -> Unit, onDelete: () -> Unit) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { it == SwipeToDismissBoxValue.EndToStart }
    )
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) onDelete()
    }
    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                label = "swipeBg"
            )
            val sc by animateFloatAsState(
                targetValue = if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart) 1f else 0.75f,
                label = "swipeScale"
            )
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(color).padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.scale(sc))
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        BookmarkCard(bookmark = bookmark, onClick = onClick)
    }
}

@Composable
private fun BookmarkCard(bookmark: Bookmark, onClick: () -> Unit) {
    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bookmark, null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("${bookmark.chapterName} · ${bookmark.chapterNumber}.${bookmark.verseNumber}",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f))
                Text(formatDate(bookmark.bookmarkedAt), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Text(bookmark.sanskritText, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Text(bookmark.translation, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun formatDate(ts: Long): String = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ts))