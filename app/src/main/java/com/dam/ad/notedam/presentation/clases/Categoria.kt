package com.dam.ad.notedam.presentation.clases

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Categoria(
    var nombre:String,
    var fecha:Date


){
    fun toJsonObject(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("nombre", nombre)
        jsonObject.put("fecha", SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(fecha))
        return jsonObject
    }
}
