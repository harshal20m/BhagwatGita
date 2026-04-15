package com.gitaapp.ui.screens.chapter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gitaapp.data.model.Chapter
import com.gitaapp.data.model.Verse
import com.gitaapp.ui.components.EmptyState
import com.gitaapp.ui.components.ReadingProgressBar
import com.gitaapp.ui.components.VerseCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterScreen(
    onVerseClick: (Int, Int) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: ChapterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            ChapterTopBar(
                title = when (val s = uiState) {
                    is ChapterUiState.Success -> s.chapter.nameTransliterated
                    else -> "Chapter"
                },
                onNavigateUp = onNavigateUp,
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        when (val state = uiState) {
            is ChapterUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is ChapterUiState.Error -> {
                EmptyState(
                    icon = "🕉",
                    title = "Could not load verses",
                    subtitle = state.message,
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                )
            }

            is ChapterUiState.Success -> {
                ChapterContent(
                    chapter = state.chapter,
                    verses = state.verses,
                    onVerseClick = onVerseClick,
                    contentPadding = innerPadding
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterTopBar(
    title: String,
    onNavigateUp: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate up"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun ChapterContent(
    chapter: Chapter,
    verses: List<Verse>,
    onVerseClick: (Int, Int) -> Unit,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Chapter header card ───────────────────────────────────────────
        item(key = "chapter_header") {
            ChapterHeaderSection(chapter = chapter)
        }

        // ── Section divider ───────────────────────────────────────────────
        item(key = "divider") {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        // ── Verses ────────────────────────────────────────────────────────
        items(
            items = verses,
            key = { verse -> verse.id }
        ) { verse ->
            VerseCard(
                verse = verse,
                onClick = { onVerseClick(verse.chapterNumber, verse.verseNumber) },
                onBookmarkClick = { /* Bookmarking from chapter list is handled in VerseScreen */ }
            )
        }
    }
}

@Composable
private fun ChapterHeaderSection(chapter: Chapter) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Chapter ${chapter.number}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = chapter.nameTransliterated,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = chapter.nameSanskrit,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = chapter.nameMeaning,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = chapter.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
        if (chapter.isStarted) {
            Spacer(modifier = Modifier.height(14.dp))
            ReadingProgressBar(
                progress = chapter.readingProgressPercent,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${chapter.verseCount} verses",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
