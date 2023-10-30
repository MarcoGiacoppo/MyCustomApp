package com.example.mycustomapp.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TVShowResponse(
    @SerializedName("results")
    val tvShows: List<TVShow> // List of TV shows obtained from the response

) : Parcelable {
    constructor() : this(mutableListOf())
}
