package com.dokeraj.androtainer.models.logos

class Logos : ArrayList<Logo>()
data class Logo(val url: String, val names: List<String>, val width: Int, val height: Int)