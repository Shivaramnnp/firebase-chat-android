package com.shivasruthi.onlyss.utils // Make sure this line is correct

fun generateChannelName(userId1: String, userId2: String): String {
    // Your logic to create a unique and consistent channel name
    return if (userId1 < userId2) {
        "${userId1}_${userId2}"
    } else {
        "${userId2}_${userId1}"
    }
}