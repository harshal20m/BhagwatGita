package com.gitaapp.ui.screens.bookmark

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitaapp.core.repository.GitaRepository
import com.gitaapp.data.model.Bookmark
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

sealed interface BookmarkUiState {
    data object Loading : BookmarkUiState
    data object Empty : BookmarkUiState

    @Immutable
    data class Success(val bookmarks: List<Bookmark>) : BookmarkUiState
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val repository: GitaRepository
) : ViewModel() {

    val uiState: StateFlow<BookmarkUiState> = repository.observeAllBookmarks()
        .map { bookmarks ->
            when {
                bookmarks.isEmpty() -> BookmarkUiState.Empty
                else -> BookmarkUiState.Success(bookmarks)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BookmarkUiState.Loading
        )

    fun removeBookmark(verseId: String) {
        viewModelScope.launch {
            repository.removeBookmark(verseId)
        }
    }
}
