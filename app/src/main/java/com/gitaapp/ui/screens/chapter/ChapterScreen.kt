package com.gitaapp.ui.screens.chapter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.gitaapp.R
import com.gitaapp.data.model.Chapter
import com.gitaapp.data.model.Verse
import com.gitaapp.ui.components.BookmarkIconButton
import com.gitaapp.ui.components.EmptyState
import com.gitaapp.ui.components.ReadingProgressBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterScreen(
    onVerseClick: (Int, Int) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: ChapterViewModel = hiltViewModel()
) {
    val uiState       by viewModel.uiState.collectAsStateWithLifecycle()
    val showOnlyVerses by viewModel.showOnlyVerses.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            ChapterTopBar(
                title = when (val s = uiState) {
                    is ChapterUiState.Success -> s.chapter.nameTransliterated
                    else -> stringResource(R.string.label_chapter)
                },
                showOnlyVerses = showOnlyVerses,
                onToggleReadMode = { viewModel.toggleReadMode() },
                onNavigateUp = onNavigateUp,
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        when (val state = uiState) {
            is ChapterUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(innerPadding), Alignment.Center
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

            is ChapterUiState.Error -> EmptyState(
                icon = "🕉", title = stringResource(R.string.error_load_verses), subtitle = state.message,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            is ChapterUiState.Success -> ChapterContent(
                chapter        = state.chapter,
                verses         = state.verses,
                showOnlyVerses = showOnlyVerses,
                onVerseClick   = onVerseClick,
                contentPadding = innerPadding
            )
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterTopBar(
    title: String,
    showOnlyVerses: Boolean,
    onToggleReadMode: () -> Unit,
    onNavigateUp: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
            }
        },
        actions = {
            // Toggle: full explanations ↔ read-only verse list
            IconButton(onClick = onToggleReadMode) {
                Icon(
                    imageVector = if (showOnlyVerses) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                    contentDescription = if (showOnlyVerses) stringResource(R.string.chapter_show_explanations) else stringResource(R.string.chapter_read_only),
                    tint = if (showOnlyVerses) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor         = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        scrollBehavior = scrollBehavior
    )
}

// ── Chapter content ───────────────────────────────────────────────────────────

@Composable
private fun ChapterContent(
    chapter: Chapter,
    verses: List<Verse>,
    showOnlyVerses: Boolean,
    onVerseClick: (Int, Int) -> Unit,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start  = 16.dp, end = 16.dp,
            top    = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Chapter header — hidden in read-only mode ─────────────────────
        item(key = "chapter_header") {
            AnimatedVisibility(
                visible = !showOnlyVerses,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                ChapterHeaderSection(chapter = chapter)
            }
        }

        // ── Read-mode banner ──────────────────────────────────────────────
        item(key = "read_mode_banner") {
            AnimatedVisibility(visible = showOnlyVerses, enter = fadeIn(), exit = fadeOut()) {
                Surface(
                    shape  = RoundedCornerShape(12.dp),
                    color  = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.MenuBook, null,
                            tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                        Text(stringResource(R.string.chapter_read_only_mode),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }

        item(key = "divider") {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 2.dp),
                color    = MaterialTheme.colorScheme.outlineVariant
            )
        }

        // ── Verse list ────────────────────────────────────────────────────
        items(items = verses, key = { it.id }) { verse ->
            if (showOnlyVerses) {
                ReadOnlyVerseCard(
                    verse   = verse,
                    onClick = { onVerseClick(verse.chapterNumber, verse.verseNumber) }
                )
            } else {
                FullVerseCard(
                    verse          = verse,
                    onVerseClick   = { onVerseClick(verse.chapterNumber, verse.verseNumber) },
                    onBookmarkClick = { /* bookmark managed in VerseScreen */ }
                )
            }
        }
    }
}

// ── Read-only card — just Sanskrit + verse number ─────────────────────────────

@Composable
private fun ReadOnlyVerseCard(verse: Verse, onClick: () -> Unit) {
    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Verse number pill
            Surface(
                shape  = RoundedCornerShape(8.dp),
                color  = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.width(34.dp)
            ) {
                Text(
                    text  = "${verse.verseNumber}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
            Text(
                text     = verse.sanskritText,
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ── Full verse card — Sanskrit + translation preview ──────────────────────────

@Composable
private fun FullVerseCard(
    verse: Verse,
    onVerseClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Card(
        onClick   = onVerseClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("${verse.chapterNumber}.${verse.verseNumber}",
                    style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                BookmarkIconButton(isBookmarked = verse.isBookmarked, onClick = onBookmarkClick)
            }
            Spacer(Modifier.height(6.dp))
            Text(verse.sanskritText, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface, maxLines = 2,
                overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Text(verse.translation, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3,
                overflow = TextOverflow.Ellipsis)
        }
    }
}

// ── Chapter header section ────────────────────────────────────────────────────

@Composable
private fun ChapterHeaderSection(chapter: Chapter) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        val chapterLabel = stringResource(R.string.label_chapter)
        Text("$chapterLabel ${chapter.number}", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(chapter.nameTransliterated, style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(2.dp))
        Text(chapter.nameSanskrit, style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(2.dp))
        Text(chapter.nameMeaning, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Text(chapter.summary, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight)
        if (chapter.isStarted) {
            Spacer(Modifier.height(14.dp))
            ReadingProgressBar(progress = chapter.readingProgressPercent, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(4.dp))
        val verseLabel = stringResource(R.string.label_verse)
        Text("${chapter.verseCount} $verseLabel", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}