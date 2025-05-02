package com.example.myapplication.info

import android.content.Context

val classInfoMap = mapOf(
    "healthy" to ("Cacao pod detected" to "Harvest the pod when it ripens."),
    "pod-rot" to ("Coffee cherry detected" to "Ensure cherries are red before harvesting."),
)

fun loadClassInfoMap(context: Context): Map<String, Pair<String, String>> {
    val json = context.assets.open("class_info.json").bufferedReader().use { it.readText() }
    val jsonArray = org.json.JSONArray(json)
    val map = mutableMapOf<String, Pair<String, String>>()

    for (i in 0 until jsonArray.length()) {
        val item = jsonArray.getJSONObject(i)
        val label = item.getString("label")
        val description = item.getString("description")
        val actionPlan = item.getString("actionPlan")
        map[label] = description to actionPlan
    }

    return map
}