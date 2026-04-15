package com.gitaapp.ui.screens.search

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitaapp.core.repository.GitaRepository
import com.gitaapp.data.model.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

sealed interface SearchUiState {
    data object Idle : SearchUiState          // No query entered yet
    data object Loading : SearchUiState       // Debounce in progress
    data object Empty : SearchUiState         // Query returned 0 results

    @Immutable
    data class Success(val results: List<SearchResult>) : SearchUiState

    data class Error(val message: String) : SearchUiState
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: GitaRepository
) : ViewModel() {

    companion object {
        private const val DEBOUNCE_MS = 300L
        private const val MIN_QUERY_LENGTH = 2
    }

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val uiState: StateFlow<SearchUiState> = _query
        .debounce(DEBOUNCE_MS)
        .flatMapLatest { q ->
            when {
                q.length < MIN_QUERY_LENGTH -> flowOf(SearchUiState.Idle)
                else -> repository.searchVerses(q).map { results ->
                    if (results.isEmpty()) SearchUiState.Empty
                    else SearchUiState.Success(results)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchUiState.Idle
        )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun clearQuery() {
        _query.value = ""
    }
}
