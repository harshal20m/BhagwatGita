package com.gitaapp.ui.screens.search

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitaapp.core.di.PreferencesManager
import com.gitaapp.core.repository.GitaRepository
import com.gitaapp.data.model.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SearchUiState {
    data object Idle    : SearchUiState
    data object Loading : SearchUiState
    data object Empty   : SearchUiState
    @Immutable data class Success(val results: List<SearchResult>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: GitaRepository,
    private val prefsManager: PreferencesManager
) : ViewModel() {

    private companion object {
        const val DEBOUNCE_MS = 200L   // Faster than before for quick-while feel
        const val MIN_LENGTH  = 2
    }

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val searchHistory: StateFlow<List<String>> = prefsManager.searchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<SearchUiState> = _query
        .debounce(DEBOUNCE_MS)
        .flatMapLatest { q ->
            when {
                q.length < MIN_LENGTH -> flowOf(SearchUiState.Idle)
                else -> repository.searchVerses(q).map { results ->
                    if (results.isEmpty()) SearchUiState.Empty else SearchUiState.Success(results)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState.Idle)

    fun onQueryChange(q: String) { _query.value = q }
    fun clearQuery() { _query.value = "" }

    fun onSearchSubmit(q: String) {
        if (q.length >= MIN_LENGTH) {
            viewModelScope.launch { prefsManager.addSearchHistory(q) }
        }
    }

    fun onHistoryItemClick(q: String) { _query.value = q }

    fun removeHistoryItem(q: String) {
        viewModelScope.launch { prefsManager.removeSearchHistory(q) }
    }

    fun clearHistory() {
        viewModelScope.launch { prefsManager.clearSearchHistory() }
    }
}
