package com.gitaapp.ui.screens.verse

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.gitaapp.R
import com.gitaapp.data.model.AppLanguage
import com.gitaapp.data.model.ReadingPreferences
import com.gitaapp.ui.components.BookmarkIconButton
import com.gitaapp.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerseScreen(
    onNavigateUp: () -> Unit,
    onNavigateToChapter: (Int) -> Unit,
    onNavigateToVerse: (Int, Int) -> Unit,
    viewModel: VerseViewModel = hiltViewModel()
) {
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val countdown by viewModel.autoNextCountdown.collectAsStateWithLifecycle()

    // Wire auto-next navigation events
    LaunchedEffect(Unit) {
        viewModel.navigateToVerse.collect { (c, v) -> onNavigateToVerse(c, v) }
    }

    Scaffold(
        topBar = {
            VerseTopBar(
                title = when (val s = uiState) {
                    is VerseUiState.Success -> "${s.chapterName} · ${s.verse.chapterNumber}.${s.verse.verseNumber}"
                    else -> stringResource(R.string.verse_label, "").trim()
                },
                isBookmarked    = (uiState as? VerseUiState.Success)?.verse?.isBookmarked ?: false,
                isFocusMode     = (uiState as? VerseUiState.Success)?.preferences?.focusModeEnabled ?: false,
                onNavigateUp    = onNavigateUp,
                onBookmarkClick = { viewModel.toggleBookmark() },
                onToggleFocus   = { viewModel.toggleFocusMode() }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is VerseUiState.Loading ->
                Box(Modifier.fillMaxSize().padding(innerPadding), Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            is VerseUiState.Error ->
                EmptyState("🕉", stringResource(R.string.error_load_verse), state.message,
                    Modifier.fillMaxSize().padding(innerPadding))
            is VerseUiState.Success ->
                VerseContent(
                    state            = state,
                    topPadding       = innerPadding.calculateTopPadding(),
                    countdown        = countdown,
                    onPreviousVerse  = {
                        viewModel.resetAutoNext()
                        onNavigateToVerse(state.verse.chapterNumber, state.verse.verseNumber - 1)
                    },
                    onNextVerse      = {
                        viewModel.resetAutoNext()
                        onNavigateToVerse(state.verse.chapterNumber, state.verse.verseNumber + 1)
                    },
                    onNavigateToChapter = { onNavigateToChapter(state.verse.chapterNumber) }
                )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerseTopBar(
    title: String, isBookmarked: Boolean, isFocusMode: Boolean,
    onNavigateUp: () -> Unit, onBookmarkClick: () -> Unit, onToggleFocus: () -> Unit
) {
    TopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleSmall) },
        navigationIcon = { IconButton(onClick = onNavigateUp) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back)) } },
        actions = {
            IconButton(onClick = onToggleFocus) {
                Icon(
                    if (isFocusMode) Icons.Filled.CenterFocusStrong else Icons.Outlined.CenterFocusWeak,
                    stringResource(R.string.focus_mode),
                    tint = if (isFocusMode) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BookmarkIconButton(isBookmarked = isBookmarked, onClick = onBookmarkClick)
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
private fun VerseContent(
    state: VerseUiState.Success,
    topPadding: androidx.compose.ui.unit.Dp,
    countdown: Int?,
    onPreviousVerse: () -> Unit,
    onNextVerse: () -> Unit,
    onNavigateToChapter: () -> Unit
) {
    val prefs   = state.preferences
    val verse   = state.verse
    val isHindi = prefs.language == AppLanguage.HINDI
    val scale   = prefs.fontSize.scale

    Box(Modifier.fillMaxSize()) {
        // ── Scrollable content ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = topPadding)
                .padding(bottom = 100.dp)   // clearance for floating nav bar
        ) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                // Verse pill
                SurfacePill("${verse.chapterNumber}.${verse.verseNumber}")
                Spacer(Modifier.height(16.dp))
                // Sanskrit — always shown, full text no clip
                SanskritBox(verse.sanskritText, scale)
                // Transliteration
                if (prefs.showTransliteration && !prefs.focusModeEnabled) {
                    Divider16()
                    SectionLabel(stringResource(R.string.section_transliteration))
                    Spacer(Modifier.height(6.dp))
                    Text(verse.transliteration,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic, fontSize = (14 * scale).sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Translation
                Divider16()
                SectionLabel(stringResource(R.string.section_translation))
                Spacer(Modifier.height(6.dp))
                Text(
                    if (isHindi) verse.commentary else verse.translation,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = (16 * scale).sp, lineHeight = (26 * scale).sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                // Word meanings (English only)
                if (prefs.showWordMeanings && !prefs.focusModeEnabled && !isHindi) {
                    Divider16()
                    SectionLabel(stringResource(R.string.section_word_meanings))
                    Spacer(Modifier.height(6.dp))
                    Text(verse.wordMeanings,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (14 * scale).sp, lineHeight = (22 * scale).sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Commentary (English only)
                if (prefs.showCommentary && !prefs.focusModeEnabled && !isHindi) {
                    Divider16()
                    SectionLabel(stringResource(R.string.section_commentary))
                    Spacer(Modifier.height(6.dp))
                    Text(verse.commentary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (14 * scale).sp, lineHeight = (22 * scale).sp),
                        color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        // ── Floating fixed navigation bar ────────────────────────────────
        FloatingVerseNav(
            verseNumber  = verse.verseNumber,
            totalVerses  = state.totalVersesInChapter,
            chapterNumber = verse.chapterNumber,
            countdown    = countdown,
            onPrev       = onPreviousVerse,
            onNext       = onNextVerse,
            onChapter    = onNavigateToChapter,
            modifier     = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ── Floating verse navigation bar ────────────────────────────────────────────

@Composable
private fun FloatingVerseNav(
    verseNumber: Int, totalVerses: Int, chapterNumber: Int,
    countdown: Int?,
    onPrev: () -> Unit, onNext: () -> Unit, onChapter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .shadow(12.dp, RoundedCornerShape(28.dp), ambientColor = MaterialTheme.colorScheme.primary.copy(0.1f))
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Prev
            FilledTonalIconButton(
                onClick  = onPrev,
                enabled  = verseNumber > 1,
                modifier = Modifier.size(42.dp)
            ) { Icon(Icons.AutoMirrored.Filled.NavigateBefore, stringResource(R.string.action_prev)) }

            // Chapter pill + verse counter
            FilledTonalButton(
                onClick = onChapter,
                shape   = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val chapterLabel = stringResource(R.string.label_chapter)
                    Text("$chapterLabel $chapterNumber", style = MaterialTheme.typography.labelMedium)
                    Text("$verseNumber / $totalVerses",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = LocalContentColor.current.copy(alpha = 0.7f))
                }
            }

            // Auto-next countdown ring (shown when enabled)
            if (countdown != null) {
                AutoNextRing(countdown = countdown, onTap = onNext)
            }

            // Next
            FilledTonalIconButton(
                onClick  = onNext,
                enabled  = verseNumber < totalVerses,
                modifier = Modifier.size(42.dp)
            ) { Icon(Icons.AutoMirrored.Filled.NavigateNext, stringResource(R.string.action_next)) }
        }
    }
}

@Composable
private fun AutoNextRing(countdown: Int, onTap: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { onTap() }
    ) {
        Text(
            text  = countdown.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ── Small helpers ─────────────────────────────────────────────────────────────

@Composable private fun SurfacePill(text: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
    }
}

@Composable private fun SanskritBox(text: String, scale: Float) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            .padding(16.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = (17 * scale).sp, lineHeight = (28 * scale).sp),
            color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable private fun Divider16() {
    Spacer(Modifier.height(20.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(Modifier.height(20.dp))
}

@Composable private fun SectionLabel(label: String) {
    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
}