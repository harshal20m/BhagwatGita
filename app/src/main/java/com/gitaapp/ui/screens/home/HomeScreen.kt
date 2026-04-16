package com.gitaapp.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gitaapp.data.model.AppLanguage
import com.gitaapp.data.model.Chapter
import com.gitaapp.data.model.ReadingProgress
import com.gitaapp.data.model.Verse
import com.gitaapp.ui.components.ChapterCardShimmer
import com.gitaapp.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onChapterClick: (Int) -> Unit,
    onContinueReading: (Int, Int) -> Unit,
    onVerseInChapterClick: (Int, Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState       by viewModel.uiState.collectAsStateWithLifecycle()
    val verseOfTheDay by viewModel.verseOfTheDay.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val isScrolling by remember { derivedStateOf { scrollBehavior.state.contentOffset != 0f && scrollBehavior.state.heightOffset != 0f && scrollBehavior.state.heightOffset != scrollBehavior.state.heightOffsetLimit } }

    // This effect ensures the header snaps back when scrolling stops
    LaunchedEffect(scrollBehavior.state.contentOffset) {
        if (scrollBehavior.state.contentOffset == 0f) {
            scrollBehavior.state.heightOffset = 0f
        }
    }

    var headerHeight by remember { mutableFloatStateOf(0f) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
            val lazyListState = rememberLazyListState()

            // Automatically snap header back when scroll stops
            LaunchedEffect(lazyListState.isScrollInProgress) {
                if (!lazyListState.isScrollInProgress) {
                    scrollBehavior.state.heightOffset = 0f
                }
            }

            LazyColumn(
                state         = lazyListState,
                modifier      = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = with(LocalDensity.current) { headerHeight.toDp() } + 8.dp,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Continue reading ──────────────────────────────────────────────
                uiState.lastReadProgress?.let { progress ->
                    item(key = "continue") {
                        ContinueReadingBanner(progress, onClick = {
                            onContinueReading(progress.chapterNumber, progress.lastReadVerseNumber)
                        }, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }

                // ── Verse of the day ──────────────────────────────────────────────
                verseOfTheDay?.let { verse ->
                    item(key = "votd") {
                        val translation = if (uiState.language == AppLanguage.HINDI) verse.translationHi else verse.translation
                        VerseOfTheDayCard(
                            verse       = verse,
                            translation = translation,
                            onRefresh   = viewModel::refreshVerseOfTheDay,
                            onClick     = { onContinueReading(verse.chapterNumber, verse.verseNumber) },
                            modifier    = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // ── Dot-matrix chapter navigator ──────────────────────────────────
                when (val st = uiState.chaptersState) {
                    is ChaptersState.Success -> {
                        item(key = "chapter_nav_header") {
                            Text("Navigate Chapters",
                                style    = MaterialTheme.typography.titleMedium,
                                color    = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
                        }
                        item(key = "dot_matrix") {
                            ChapterDotMatrix(
                                chapters         = st.chapters,
                                onChapterClick   = onChapterClick,
                                onVerseClick     = onVerseInChapterClick,
                                modifier         = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        item(key = "chapters_list_header") {
                            Text("All Chapters",
                                style    = MaterialTheme.typography.titleMedium,
                                color    = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
                        }
                        items(items = st.chapters, key = { it.number }) { chapter ->
                            ChapterRowCard(chapter = chapter, onClick = { onChapterClick(chapter.number) },
                                modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                    is ChaptersState.Loading -> {
                        items(count = 6, key = { "sh_$it" }) {
                            ChapterCardShimmer(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                    is ChaptersState.Error -> item {
                        EmptyState("🕉", "Could not load chapters", st.message)
                    }
                }
            }

            // Smoothly hiding header
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationY = scrollBehavior.state.heightOffset }
                    .onGloballyPositioned { layoutCoordinates ->
                        val height = layoutCoordinates.size.height.toFloat()
                        headerHeight = height
                        if (scrollBehavior.state.heightOffsetLimit != -height) {
                            scrollBehavior.state.heightOffsetLimit = -height
                        }
                    }
            ) {
                Column(modifier = Modifier.statusBarsPadding().padding(top = 16.dp, bottom = 12.dp)) {
                    GitaHeader(overallProgress = uiState.overallProgress)
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun GitaHeader(overallProgress: Float) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("श्रीमद्भगवद्गीता",
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text("Bhagavad Gita",
            style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold)
        if (overallProgress > 0f) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LinearProgressIndicator(
                    progress  = { overallProgress },
                    modifier  = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)),
                    color     = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                    strokeCap = StrokeCap.Round
                )
                Text("${(overallProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ── Continue Reading Banner ───────────────────────────────────────────────────

@Composable
private fun ContinueReadingBanner(progress: ReadingProgress, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Continue Reading", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                Spacer(Modifier.height(2.dp))
                Text("Chapter ${progress.chapterNumber} · Verse ${progress.lastReadVerseNumber}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress  = { progress.progressPercent },
                    modifier  = Modifier.fillMaxWidth(0.7f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color     = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
        }
    }
}

// ── Verse of the Day ──────────────────────────────────────────────────────────

@Composable
private fun VerseOfTheDayCard(
    verse: Verse,
    translation: String,
    onRefresh: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Verse of the Day", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                }
                IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text("${verse.chapterNumber}.${verse.verseNumber}", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(4.dp))
            // Sanskrit — no maxLines clip
            Text(verse.sanskritText, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f))
            Spacer(Modifier.height(10.dp))
            // Full translation (Hindi/English based on settings)
            Text(translation, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f))
        }
    }
}

// ── Chapter Dot Matrix Navigator ──────────────────────────────────────────────

@Composable
private fun ChapterDotMatrix(
    chapters: List<Chapter>,
    onChapterClick: (Int) -> Unit,
    onVerseClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedChapter by remember { mutableIntStateOf(-1) }

    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(16.dp)) {
            // Chapter dots row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                chapters.forEach { chapter ->
                    val isExpanded  = expandedChapter == chapter.number
                    val isStarted   = chapter.isStarted
                    val isComplete  = chapter.readingProgressPercent >= 1f
                    val alpha by animateFloatAsState(if (isExpanded) 1f else 0.85f, label = "dotAlpha")

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            expandedChapter = if (isExpanded) -1 else chapter.number
                        }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(if (isExpanded) 42.dp else 36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isComplete  -> MaterialTheme.colorScheme.primary
                                        isStarted   -> MaterialTheme.colorScheme.primaryContainer
                                        isExpanded  -> MaterialTheme.colorScheme.secondaryContainer
                                        else        -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .alpha(alpha)
                        ) {
                            Text(
                                text  = chapter.number.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isComplete -> MaterialTheme.colorScheme.onPrimary
                                    isStarted  -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else       -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        if (isStarted && !isComplete) {
                            Spacer(Modifier.height(3.dp))
                            Box(Modifier.size(4.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary))
                        }
                    }
                }
            }

            // Expandable verse dots for selected chapter
            val selected = chapters.firstOrNull { it.number == expandedChapter }
            AnimatedVisibility(
                visible = selected != null,
                enter = expandVertically(tween(250)) + fadeIn(tween(200)),
                exit  = shrinkVertically(tween(200)) + fadeOut(tween(150))
            ) {
                selected?.let { ch ->
                    Column {
                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(ch.nameTransliterated, style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface)
                                Text(ch.nameSanskrit, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                            TextButton(onClick = { onChapterClick(ch.number) }) {
                                Text("Open", style = MaterialTheme.typography.labelMedium)
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(14.dp))
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        // Verse dot matrix — all verses as small tappable dots
                        VerseDotGrid(
                            chapter      = ch,
                            onVerseClick = { v -> onVerseClick(ch.number, v) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VerseDotGrid(chapter: Chapter, onVerseClick: (Int) -> Unit) {
    val lastRead = (chapter.readingProgressPercent * chapter.verseCount).toInt()
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement   = Arrangement.spacedBy(6.dp),
        modifier              = Modifier.fillMaxWidth()
    ) {
        (1..chapter.verseCount).forEach { v ->
            val isRead = v <= lastRead
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isRead) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onVerseClick(v) }
            ) {
                Text(
                    text  = v.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (isRead) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Spacer(Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LegendDot(MaterialTheme.colorScheme.primary, "Read")
        LegendDot(MaterialTheme.colorScheme.surfaceVariant, "Unread")
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Chapter Row Card (compact list) ──────────────────────────────────────────

@Composable
private fun ChapterRowCard(chapter: Chapter, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center) {
                Text(chapter.number.toString(), style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(chapter.nameTransliterated, style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(chapter.nameSanskrit, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${chapter.verseCount}v", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (chapter.isStarted) {
                    Spacer(Modifier.height(4.dp))
                    Text("${(chapter.readingProgressPercent * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}