package com.necatisozer.mvvmshowcase.movielist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necatisozer.mvvmshowcase.data.Movie
import com.necatisozer.mvvmshowcase.data.UiState
import com.necatisozer.mvvmshowcase.data.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import mvvmshowcase.composeapp.generated.resources.Res
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class MovieListViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val searchQuery = savedStateHandle.getMutableStateFlow("searchQuery", "")

    private val movies: MutableStateFlow<UiState<List<Movie>>> = MutableStateFlow(UiState.Loading)

    val uiState = combine(searchQuery, movies) { searchQuery, movies ->
        val queriedMovies = if (searchQuery.isBlank()) {
            movies
        } else {
            movies.map { movieList ->
                movieList.filter { it.title.contains(searchQuery, ignoreCase = true) }
            }
        }

        MovieListUiState(searchQuery = searchQuery, movies = queriedMovies)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MovieListUiState()
    )

    init {
        fetchMovies()
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.update { query }
    }

    fun onRetryClick() {
        fetchMovies()
    }

    fun fetchMovies() {
        viewModelScope.launch {
            movies.update { UiState.Loading }
            delay(1.seconds)
            if (Random.nextFloat() < 0.2f) {
                movies.update { UiState.Failure(RuntimeException("Network Error")) }
                return@launch
            }
            val jsonString = Res.readBytes("files/movies.json").decodeToString()
            val movieList: List<Movie> = Json.decodeFromString(jsonString)
            movies.update { UiState.Success(movieList) }
        }
    }
}