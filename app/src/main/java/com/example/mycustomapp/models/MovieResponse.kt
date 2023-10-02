package com.example.mycustomapp.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MovieResponse(
    @SerializedName("results")
    val movies : List<Movie> // List of movies obtained from the response

) : Parcelable {
    constructor() : this(mutableListOf())
}


