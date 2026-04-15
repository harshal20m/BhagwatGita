package com.gitaapp.ui.screens.verse

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gitaapp.data.model.ReadingPreferences
import com.gitaapp.data.model.Verse
import com.gitaapp.ui.components.BookmarkIconButton
import com.gitaapp.ui.components.EmptyState
import com.gitaapp.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerseScreen(
    onNavigateUp: () -> Unit,
    onNavigateToChapter: (Int) -> Unit,
    onNavigateToVerse: (Int, Int) -> Unit,
    viewModel: VerseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            VerseTopBar(
                title = when (val s = uiState) {
                    is VerseUiState.Success ->
                        "${s.chapterName} · ${s.verse.chapterNumber}.${s.verse.verseNumber}"
                    else -> "Verse"
                },
                isBookmarked = (uiState as? VerseUiState.Success)?.verse?.isBookmarked ?: false,
                onNavigateUp = onNavigateUp,
                onBookmarkClick = { viewModel.toggleBookmark() }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is VerseUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is VerseUiState.Error -> {
                EmptyState(
                    icon = "🕉",
                    title = "Could not load verse",
                    subtitle = state.message,
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                )
            }

            is VerseUiState.Success -> {
                VerseContent(
                    state = state,
                    contentPadding = innerPadding,
                    onPreviousVerse = {
                        onNavigateToVerse(state.verse.chapterNumber, state.verse.verseNumber - 1)
                    },
                    onNextVerse = {
                        onNavigateToVerse(state.verse.chapterNumber, state.verse.verseNumber + 1)
                    },
                    onNavigateToChapter = { onNavigateToChapter(state.verse.chapterNumber) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerseTopBar(
    title: String,
    isBookmarked: Boolean,
    onNavigateUp: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall
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
        actions = {
            BookmarkIconButton(
                isBookmarked = isBookmarked,
                onClick = onBookmarkClick
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun VerseContent(
    state: VerseUiState.Success,
    contentPadding: PaddingValues,
    onPreviousVerse: () -> Unit,
    onNextVerse: () -> Unit,
    onNavigateToChapter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // ── Verse number ──────────────────────────────────────────────
            Text(
                text = "Verse ${state.verse.chapterNumber}.${state.verse.verseNumber}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ── Sanskrit Shloka ───────────────────────────────────────────
            SanskritSection(text = state.verse.sanskritText)

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(20.dp))

            // ── Transliteration ───────────────────────────────────────────
            if (state.preferences.showTransliteration) {
                TransliterationSection(text = state.verse.transliteration)
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── English Translation ───────────────────────────────────────
            TranslationSection(text = state.verse.translation)

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(20.dp))

            // ── Word Meanings ─────────────────────────────────────────────
            if (state.preferences.showWordMeanings) {
                WordMeaningsSection(text = state.verse.wordMeanings)
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Hindi Commentary ──────────────────────────────────────────
            if (state.preferences.showCommentary) {
                CommentarySection(text = state.verse.commentary)
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Navigation buttons ────────────────────────────────────────
            VerseNavigationRow(
                verseNumber = state.verse.verseNumber,
                totalVerses = state.totalVersesInChapter,
                chapterNumber = state.verse.chapterNumber,
                onPreviousVerse = onPreviousVerse,
                onNextVerse = onNextVerse,
                onNavigateToChapter = onNavigateToChapter
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Sanskrit Section ──────────────────────────────────────────────────────────

@Composable
private fun SanskritSection(text: String) {
    Column {
        SectionLabel(label = "Sanskrit")
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                .padding(16.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }
    }
}

// ── Transliteration Section ───────────────────────────────────────────────────

@Composable
private fun TransliterationSection(text: String) {
    Column {
        SectionLabel(label = "Transliteration")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}

// ── Translation Section ───────────────────────────────────────────────────────

@Composable
private fun TranslationSection(text: String) {
    Column {
        SectionLabel(label = "Translation")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )
    }
}

// ── Word Meanings Section ─────────────────────────────────────────────────────

@Composable
private fun WordMeaningsSection(text: String) {
    Column {
        SectionLabel(label = "Word Meanings")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}

// ── Commentary Section ────────────────────────────────────────────────────────

@Composable
private fun CommentarySection(text: String) {
    Column {
        SectionLabel(label = "Commentary (हिन्दी)")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}

// ── Section Label ─────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(label: String) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing
    )
}

// ── Verse Navigation Row ──────────────────────────────────────────────────────

@Composable
private fun VerseNavigationRow(
    verseNumber: Int,
    totalVerses: Int,
    chapterNumber: Int,
    onPreviousVerse: () -> Unit,
    onNextVerse: () -> Unit,
    onNavigateToChapter: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$verseNumber / $totalVerses",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = onPreviousVerse,
                enabled = verseNumber > 1
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                    contentDescription = "Previous verse"
                )
            }

            FilledTonalButton(onClick = onNavigateToChapter) {
                Text(
                    text = "Chapter $chapterNumber",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            FilledTonalIconButton(
                onClick = onNextVerse,
                enabled = verseNumber < totalVerses
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                    contentDescription = "Next verse"
                )
            }
        }
    }
}
