package com.gitaapp.ui.screens.search

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gitaapp.data.model.SearchResult
import com.gitaapp.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onVerseClick: (Int, Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query   by viewModel.query.collectAsStateWithLifecycle()
    val history by viewModel.searchHistory.collectAsStateWithLifecycle()
    val kb = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize().padding(bottom = 100.dp)) {
        // ── Top bar ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Search", style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            AnimatedVisibility(visible = history.isNotEmpty() && query.isBlank()) {
                TextButton(onClick = { viewModel.clearHistory() }) {
                    Text("Clear history", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // ── Search field ──────────────────────────────────────────────────
        OutlinedTextField(
            value = query, onValueChange = { viewModel.onQueryChange(it) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            placeholder = { Text("Search 701 verses…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = {
                AnimatedVisibility(query.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                    IconButton(onClick = { viewModel.clearQuery() }) {
                        Icon(Icons.Default.Clear, "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                kb?.hide()
                viewModel.onSearchSubmit(query)
            }),
            shape  = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor   = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            textStyle = MaterialTheme.typography.bodyMedium
        )

        // ── Results / history ─────────────────────────────────────────────
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
            label = "searchContent"
        ) { state ->
            when (state) {
                SearchUiState.Idle -> {
                    if (history.isEmpty()) {
                        IdleHint(modifier = Modifier.fillMaxSize())
                    } else {
                        SearchHistoryList(
                            history     = history,
                            onItemClick = { viewModel.onHistoryItemClick(it) },
                            onRemove    = { viewModel.removeHistoryItem(it) }
                        )
                    }
                }
                SearchUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                }
                SearchUiState.Empty -> EmptyState("🔍", "No results found",
                    "Try different keywords — translations, transliterations, or Sanskrit terms.",
                    modifier = Modifier.fillMaxSize())
                is SearchUiState.Success -> ResultsList(state.results, query, onVerseClick,
                    onSubmit = { viewModel.onSearchSubmit(query) })
                is SearchUiState.Error   -> EmptyState("⚠️", "Search error", state.message,
                    modifier = Modifier.fillMaxSize())
            }
        }
    }
}

// ── Search History ────────────────────────────────────────────────────────────

@Composable
private fun SearchHistoryList(history: List<String>, onItemClick: (String) -> Unit, onRemove: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        item(key = "hist_label") {
            Text("Recent searches", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp))
        }
        items(items = history, key = { it }) { term ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable { onItemClick(term) }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Outlined.History, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp))
                Text(term, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Icon(Icons.Default.NorthWest, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp).clickable { onItemClick(term) })
                IconButton(onClick = { onRemove(term) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp))
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}

// ── Idle hint ─────────────────────────────────────────────────────────────────

@Composable
private fun IdleHint(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(top = 56.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🕉", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(16.dp))
        Text("Search the Gita", style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        Text("Search across all 701 verses by keyword, phrase, or theme",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        ChipRow(listOf("duty", "karma", "soul", "devotion", "knowledge", "peace", "dharma", "yoga"))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRow(items: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEach { term ->
            SuggestionChip(
                onClick = {},
                label   = { Text(term, style = MaterialTheme.typography.labelMedium) },
                colors  = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor     = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}

// ── Results list ──────────────────────────────────────────────────────────────

@Composable
private fun ResultsList(
    results: List<SearchResult>, query: String,
    onVerseClick: (Int, Int) -> Unit, onSubmit: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item(key = "count") {
            Text("${results.size} result${if (results.size != 1) "s" else ""}",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 2.dp))
        }
        items(items = results, key = { it.verseId }) { result ->
            ResultCard(result = result, query = query,
                onClick = { onVerseClick(result.chapterNumber, result.verseNumber); onSubmit() })
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun ResultCard(result: SearchResult, query: String, onClick: () -> Unit) {
    Card(
        onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(result.verseId, style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("·", color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.width(8.dp))
                Text(result.chapterName, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(8.dp))
            HighlightedText(result.matchHighlight, query,
                MaterialTheme.typography.bodyMedium, MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun HighlightedText(
    text: String, query: String,
    style: androidx.compose.ui.text.TextStyle,
    color: androidx.compose.ui.graphics.Color
) {
    val highlight = MaterialTheme.colorScheme.primary
    val annotated = remember(text, query) {
        buildAnnotatedString {
            if (query.isBlank()) { append(text); return@buildAnnotatedString }
            var idx = 0
            val lo = text.lowercase(); val lq = query.lowercase()
            while (idx < text.length) {
                val m = lo.indexOf(lq, idx)
                if (m == -1) { append(text.substring(idx)); break }
                append(text.substring(idx, m))
                withStyle(SpanStyle(color = highlight, fontWeight = FontWeight.SemiBold,
                    background = highlight.copy(alpha = 0.12f))) {
                    append(text.substring(m, m + query.length))
                }
                idx = m + query.length
            }
        }
    }
    Text(annotated, style = style, color = color, maxLines = 4, overflow = TextOverflow.Ellipsis)
}