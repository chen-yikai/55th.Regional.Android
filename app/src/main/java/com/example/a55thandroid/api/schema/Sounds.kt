package com.example.a55thandroid.api.schema

data class Sounds(
    val id: Int,
    val name: String,
    val metadata: MetaData,
    val audio: Audio,
    val cover: Cover
)

data class MetaData(
    val description: String,
    val tags: List<String>,
    val author: String,
    val lastUpdated: String,
    val details: String,
    val publishDate: String,
)

data class Audio(
    val url: String,
    val format: String,
    val duration: Int
)

data class Cover(
    val url: String
)