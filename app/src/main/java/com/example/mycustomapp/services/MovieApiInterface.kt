package com.example.mycustomapp.services

import com.example.mycustomapp.models.Movie
import com.example.mycustomapp.models.MovieResponse
import com.example.mycustomapp.models.TVShowResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.nio.channels.CancelledKeyException

// API request for Popular movies

// API Request for 1st page
interface MovieApiInterface {
    @GET("/3/movie/popular?api_key=381e5879afdcdcba913bc1f839a6f004&page=1")
    fun getMovieList(): Call<MovieResponse>
}

// API Request for 2nd page
interface MovieApiInterface2 {
    @GET("/3/movie/popular?api_key=381e5879afdcdcba913bc1f839a6f004&page=2")
    fun getMovieList(): Call<MovieResponse>
}

// API Request for 3rd page
interface MovieApiInterface3 {
    @GET("/3/movie/popular?api_key=381e5879afdcdcba913bc1f839a6f004&page=3")
    fun getMovieList(): Call<MovieResponse>
}

// Interface for Top Rated movies
// Page 1
interface TopRatedMovieInterface1 {
    @GET("/3/movie/top_rated?api_key=381e5879afdcdcba913bc1f839a6f004&page=1")
    fun getMovieList(): Call<MovieResponse>
}

// Page 2
interface TopRatedMovieInterface2 {
    @GET("/3/movie/top_rated?api_key=381e5879afdcdcba913bc1f839a6f004&page=2")
    fun getMovieList(): Call<MovieResponse>
}

// Interface for popular TV Shows
// Page 1
interface popularTVshowInterface1 {
    @GET("/3/tv/popular?api_key=381e5879afdcdcba913bc1f839a6f004&page=1")
    fun getTVlist(): Call<TVShowResponse>
}

// Page 2
interface popularTVshowInterface2 {
    @GET("/3/tv/popular?api_key=381e5879afdcdcba913bc1f839a6f004&page=2")
    fun getTVlist(): Call<TVShowResponse>
}

// Interface for Top Rated TV Show
// Page 1
interface topRatedTVshowInterface1 {
    @GET("/3/tv/top_rated?api_key=381e5879afdcdcba913bc1f839a6f004&page=1")
    fun getTVlist(): Call<TVShowResponse>
}

// Page 2
interface topRatedTVshowInterface2 {
    @GET("/3/tv/top_rated?api_key=381e5879afdcdcba913bc1f839a6f004&page=2")
    fun getTVlist(): Call<TVShowResponse>
}


// Interface for recommended movies
interface RecommendationMovie {
    @GET("movie/{movie_id}/recommendations")
    fun getMovies(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Call<MovieResponse>
}

// API Service for Playing Now movies
// Page 1
interface PlayingNowMovies {
    @GET("movie/now_playing")
    fun getNowPlayingMovies(
        @Query("api_key") apiKey: String
    ): Call<MovieResponse>
}

// Interface for Upcoming movies
interface UpcomingMovie {
    @GET("movie/upcoming")
    fun getUpcomingMovies(
        @Query("api_key") apiKey: String
    ): Call<MovieResponse>
}

