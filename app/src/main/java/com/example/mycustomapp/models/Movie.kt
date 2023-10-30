package com.example.mycustomapp.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Movie(
    @SerializedName("id")
    val id : Int?, // Movie ID

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
    constructor() : this(null,"","","","","","")
}

data class WatchlistItem(
    var key: String?, // Field to store the Firebase-generated key
    var userId: String?,
    val movieTitle: String,
    val userRating: Float,
    val userReview: String,
    val posterUrl: String?,
    val id: Int?
) {
    constructor() : this(null, null, "", 0.0f, "", "", 0)
}

data class UserProfile(
    val userId: String,
    val displayName: String?,
    val email: String?
) {
    constructor() : this("","","")
}

@Parcelize
data class TVShow(
    @SerializedName("id")
    val id : Int?, // Tv show ID

    @SerializedName("name")
    val name : String?, // TV show name

    @SerializedName("poster_path")
    val poster : String?, // URL of the movie poster

    @SerializedName("vote_average")
    val vote : String?, // Movie rating

    @SerializedName("first_air_date")
    val date : String?, // Release date of the TV Show

    @SerializedName("overview")
    val detail : String?, // TV Show description

    @SerializedName("vote_count")
    val numbers : String? // Vote Count

) : Parcelable{
    constructor() : this(null,"","","","","","")
}



