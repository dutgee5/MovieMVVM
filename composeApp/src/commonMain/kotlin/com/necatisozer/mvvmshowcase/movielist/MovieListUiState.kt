package com.necatisozer.mvvmshowcase.movielist

import com.necatisozer.mvvmshowcase.data.Movie
import com.necatisozer.mvvmshowcase.data.UiState

data class MovieListUiState(
    val movies: UiState<List<Movie>> = UiState.Loading,
    val searchQuery: String = "",
)
