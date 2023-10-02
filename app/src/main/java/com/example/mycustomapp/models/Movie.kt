package com.example.mycustomapp.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Movie(
    @SerializedName("id")
    val id : String?, // Movie ID

    @SerializedName("title")
    val title : String?, // Movie Title

    @SerializedName("poster_path")
    val poster : String?, // URL of the movie poster

    @SerializedName("vote_average")
    val vote : String?, // Movie rating

    @SerializedName("release_date")
    val date : String?, // Release date of the movie

    @SerializedName("overview")
    val detail : String?, // Movie description

    @SerializedName("vote_count")
    val numbers : String? // Vote Count

) : Parcelable{
    constructor() : this("","","","","","","")
}

data class WatchlistItem(
    var key: String?, // Field to store the Firebase-generated key
    val movieTitle: String,
    val userRating: Float,
    val userReview: String,
    val posterUrl: String?
) {
    // Default, no-argument constructor required by Firebase
    constructor() : this(null, "", 0.0f, "", "")
}


