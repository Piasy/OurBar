package com.piasy.ourbar.data

import kotlinx.serialization.Serializable

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/17.
 */
@Serializable
data class FilmInfo(
  val name: String,
  val year: String,
  val mainPic: String,
  val duration: String,
  val rating: String,
)
