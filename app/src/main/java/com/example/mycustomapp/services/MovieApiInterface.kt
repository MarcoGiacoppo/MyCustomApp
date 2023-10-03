package com.example.mycustomapp.services

import com.example.mycustomapp.models.MovieResponse
import retrofit2.Call
import retrofit2.http.GET

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

interface MovieApiInterface3 {
    @GET("/3/movie/popular?api_key=381e5879afdcdcba913bc1f839a6f004&page=3")
    fun getMovieList(): Call<MovieResponse>
}
// Can add more interface + add more functions in MainActivity to get more pages