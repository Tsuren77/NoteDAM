package com.dam.ad.notedam.presentation.clases

import java.util.Date

data class Tarea(
    var categoria:Categoria,
    var completada:Boolean,
    var fecha:Date,
    var texto:String,
    var imagen:String,
    var audio:String,
    var sublista:List<Sublista>
)
