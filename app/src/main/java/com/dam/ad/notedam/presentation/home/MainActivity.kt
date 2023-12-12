package com.dam.ad.notedam.presentation.home

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dam.ad.notedam.R
import com.dam.ad.notedam.databinding.ActivityMainBinding
import com.dam.ad.notedam.presentation.clases.Categoria
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController:NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavMenu()
        elegirDestinoDialog()
    }

    private fun initNavMenu() {
        val navHost = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHost.navController
        binding.bottomNavView.setupWithNavController(navController)
    }

    private fun elegirDestinoDialog() {
        val locations = arrayOf("Local")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona la localización")
            .setItems(locations) { _, _ ->
                elegirFormatoDialog("Local")
            }
        val dialog = builder.create()
        dialog.show()
    }
    private fun elegirFormatoDialog(location: String) {
        val formats = arrayOf("CSV", "JSON")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona el formato")
            .setItems(formats) { _, which ->
                val selectedFormat = formats[which]
                saveFileExtension(selectedFormat)

                when (selectedFormat) {
                    "CSV" -> createCSVFile(location)
                    "JSON" -> createJSONFile(location)
                }
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun createCSVFile(location: String) {
        var fileName = "listaCategorias"
        val fileContent = ""
        val fileExtension = getFileExtension()
        if(fileExtension.equals("CSV"))
            fileName+=".csv"
        val file = File(getExternalFilesDir(null), fileName)
        if (file.exists()) {
                readCSVFile(location)
        }
        else {
            FileWriter(file).use {
                it.write(fileContent)
            }
        }
        showMessage("Se ha creado el archivo $fileName en $location")
    }

    private fun createJSONFile(location: String) {
        var fileName = "listaCategorias"
        val fileExtension = getFileExtension()
        if(fileExtension.equals("JSON"))
            fileName+=".json"
        val file = File(getExternalFilesDir(null), fileName)
        if (file.exists()) {
            readJSONFile(location)
        } else {
            val jsonArray = JSONArray()
            FileWriter(file).use {
                it.write(jsonArray.toString())
            }
            showMessage("Se ha creado el archivo $fileName en $location")
        }
    }

    private fun readCSVFile(location: String): List<Categoria> {
        val fileExtension = getFileExtension()
        var fileName=""
        if(fileExtension.equals("CSV"))
            fileName = "listaCategorias.csv"
        val file = File(getExternalFilesDir(null), "$fileName")
        val categorias = mutableListOf<Categoria>()

        if (file.exists()) {
            BufferedReader(FileReader(file)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    val parts = line.split(",")
                    if (parts.size == 2) {
                        val nombre = parts[0].trim()
                        val fecha = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(parts[1].trim())
                        categorias.add(Categoria(nombre, fecha))
                    }
                    line = reader.readLine()
                }
            }
        } else {
            showMessage("El archivo $fileName no existe en $location con extensión $fileExtension")
        }

        return categorias
    }

    private fun getCurrentDate(): Date {
        return Date()
    }

    private fun readJSONFile(location: String): List<Categoria> {
        val fileExtension = getFileExtension()
        var fileName=""
        if(fileExtension.equals("JSON"))
            fileName = "listaCategorias.json"
        val file = File(getExternalFilesDir(null), "$fileName$fileExtension")
        val categorias = mutableListOf<Categoria>()
        if (file.exists()) {
            try {
                BufferedReader(FileReader(file)).use { reader ->
                    val jsonString = reader.readText()
                    val jsonArray = JSONArray(jsonString)

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)

                        val nombreObject = jsonObject.getJSONObject("nombre")
                        var nombre = nombreObject.getString("nombre")
                        showMessage("nombre"+nombre.toString().replace("[\\[\\':]", ""))
                        val date =getCurrentDate()
                        categorias.add(Categoria(nombre,date))
                    }
                }
            } catch (e: Exception) {
                showMessage("Error al leer el archivo $fileName con extensión $fileExtension: ${e.message}")
            }
        } else {
            showMessage("El archivo $fileName no existe en $location con extensión $fileExtension")
        }
        return categorias
    }

    private fun showMessage(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setPositiveButton("Aceptar", DialogInterface.OnClickListener { _, _ ->
            })
        val dialog = builder.create()
        dialog.show()
    }

    fun saveFileExtension(extension: String) {
        val sharedPrefs = getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putString("file_extension", extension)
        editor.apply()
    }

    fun getFileExtension(): String {
        val sharedPrefs = getPreferences(Context.MODE_PRIVATE)
        return sharedPrefs.getString("file_extension", "CSV") ?: "CSV"
    }

}