package com.example.mycustomapp.services

import com.example.mycustomapp.models.MovieResponse
import retrofit2.Call
import retrofit2.http.GET

interface MovieApiInterface {

    @GET("/3/movie/popular?api_key=381e5879afdcdcba913bc1f839a6f004&page=1")
    fun getMovieList(): Call<MovieResponse>
}