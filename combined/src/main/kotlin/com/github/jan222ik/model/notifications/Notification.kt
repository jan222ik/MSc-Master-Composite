package com.github.jan222ik.model.notifications

import kotlin.random.Random

data class Notification(val title: String, val message: String, val decayTimeMilli: Long?, val id: Int = Random.nextInt())