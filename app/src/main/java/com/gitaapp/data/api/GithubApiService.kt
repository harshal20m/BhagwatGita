package com.gitaapp.data.api

import retrofit2.http.GET
import com.google.gson.annotations.SerializedName

interface GithubApiService {
    @GET("repos/harshal20m/BhagwatGita/releases/latest")
    suspend fun getLatestRelease(): GithubRelease
}

data class GithubRelease(
    @SerializedName("tag_name") val tagName: String,
    @SerializedName("body") val body: String?,
    @SerializedName("html_url") val htmlUrl: String,
    val assets: List<GithubAsset>
)

data class GithubAsset(
    val name: String,
    @SerializedName("browser_download_url") val downloadUrl: String
)