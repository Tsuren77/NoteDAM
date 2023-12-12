package com.dam.ad.notedam.presentation.categories

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dam.ad.notedam.R
import com.dam.ad.notedam.databinding.FragmentCategoriesBinding
import com.dam.ad.notedam.presentation.adapter.CategoriaAdapter
import com.dam.ad.notedam.presentation.clases.Categoria
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class CategoriesFragment : Fragment() {

    private var _binding:FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCategoriesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val buttonAddCategory = binding.buttonAddCategory
        buttonAddCategory.setOnClickListener {
            when (getFileExtension()) {
                "CSV" -> addCategoryCSV()
                "JSON" -> addCategoryJSON()
            }
        }
        val buttonDeleteCategories = binding.root.findViewById<Button>(R.id.buttonDeleteCategories)
        buttonDeleteCategories.setOnClickListener {
            val selectedCategories = (binding.recyclerViewCategories.adapter as? CategoriaAdapter)?.getSelectedCategories()
            deleteSelectedCategories(selectedCategories)
        }
        val categories = readCategoriesFromFile()
        displayCategories(categories)
    }
    private fun getFileExtension(): String {
        val sharedPrefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
        return sharedPrefs.getString("file_extension", "CSV") ?: "CSV"
    }
    private fun readCategoriesFromFile(): MutableList<Categoria> {
        val fileExtension = getFileExtension()
        var fileName = ""
        if (fileExtension.equals("JSON")) {
            fileName = "listaCategorias.json"
            return readJSONCategoriesFromFile(fileName)
        } else if (fileExtension.equals("CSV")) {
            fileName = "listaCategorias.csv"
            return readCSVCategoriesFromFile(fileName)
        }
        showMessage("Extensión de archivo no válida: $fileExtension")
        return mutableListOf()
    }
    private fun readJSONCategoriesFromFile(fileName: String): MutableList<Categoria> {
        val file = File(requireContext().getExternalFilesDir(null), fileName)
        val categories = mutableListOf<Categoria>()
        if (file.exists()) {
            try {
                BufferedReader(FileReader(file)).use { reader ->
                    val jsonString = reader.readText()
                    val jsonArray = JSONArray(jsonString)
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val nombre = jsonObject.getString("nombre")
                        val fechaString = jsonObject.getString("fecha")
                        val date = convertStringToDate(fechaString)
                        if (date != null) {
                            categories.add(Categoria(nombre, date))
                        } else {
                            showMessage("Error al parsear la fecha en el archivo JSON. Fecha problemática: $fechaString.")
                        }
                    }
                }
            } catch (e: Exception) {
                showMessage("Error al leer el archivo $fileName: ${e.message}")
            }
        } else {
            showMessage("El archivo $fileName no existe")
        }
        return categories
    }
    private fun readCSVCategoriesFromFile(fileName: String): MutableList<Categoria> {
        val file = File(requireContext().getExternalFilesDir(null), fileName)
        val categories = mutableListOf<Categoria>()
        if (file.exists()) {
            try {
                BufferedReader(FileReader(file)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        val parts = line.split(",")
                        if (parts.size == 2) {
                            val nombre = parts[0].trim()
                            val fechaString = parts[1].trim()
                            val date = convertStringToDate(fechaString)
                            if (date != null) {
                                categories.add(Categoria(nombre, date))
                            } else {
                                showMessage("Error al parsear la fecha en el archivo CSV. Fecha problemática: $fechaString.")
                            }
                        }
                        line = reader.readLine()
                    }
                }
            } catch (e: Exception) {
                showMessage("Error al leer el archivo $fileName: ${e.message}")
            }
        } else {
            showMessage("El archivo $fileName no existe")
        }
        return categories
    }
    private fun convertStringToDate(dateString: String): Date? {
        val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return try {
            format.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
    private fun displayCategories(categories: List<Categoria>) {
        val recyclerView = binding.recyclerViewCategories
        val adapter = CategoriaAdapter(categories)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
    private fun addCategoryCSV() {
        showAddCategoryDialog("CSV")
    }
    private fun addCategoryJSON() {
        showAddCategoryDialog("JSON")
    }
    private fun showAddCategoryDialog(fileExtension: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextCategoryName)
        val buttonAdd = dialogView.findViewById<Button>(R.id.buttonAdd)
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setView(dialogView)
        val alertDialog = alertDialogBuilder.create()
        buttonAdd.setOnClickListener {
            val categoryName = editTextName.text.toString()
            if (categoryName.isNotEmpty()) {
                when (fileExtension) {
                    "CSV" -> addCategoryToCSV(categoryName)
                    "JSON" -> addCategoryToJSON(categoryName)
                }
                alertDialog.dismiss()
            } else {
                showToast("Por favor, introduce un nombre de categoría.")
            }
        }
        alertDialog.show()
    }
    private fun addCategoryToCSV(categoryName: String) {
        val fileExtension = getFileExtension()
        var fileName=""
        if(fileExtension.equals("CSV"))
            fileName = "listaCategorias.csv"
        val file = File(requireContext().getExternalFilesDir(null), "$fileName")
        try {
            FileWriter(file, true).use { writer ->
                writer.append("$categoryName,${getCurrentDateAsString()}\n")
            }
            showToast("Categoría '$categoryName' añadida al archivo CSV.")
            val categories = readCategoriesFromFile()
            displayCategories(categories)
        } catch (e: IOException) {
            showToast("Error al añadir la categoría al archivo CSV: ${e.message}")
        }
    }
    private fun getCurrentDate(): Date {
        return Date() // Esto crea un objeto Date con la fecha y hora actuales
    }
    private fun addCategoryToJSON(categoryName: String) {
        val fileExtension = getFileExtension()
        var fileName = ""
        if (fileExtension.equals("JSON"))
            fileName = "listaCategorias.json"
        val file = File(requireContext().getExternalFilesDir(null), "$fileName")
        try {
            val jsonArray = readJSONFileContents(file)
            val newCategoryObject = JSONObject()
            newCategoryObject.put("nombre", categoryName)
            newCategoryObject.put("fecha", getCurrentDateAsString())
            jsonArray.put(newCategoryObject)
            writeJSONFileContents(file, jsonArray)
            showToast("Categoría '$categoryName' añadida al archivo JSON.")
            val categories = readCategoriesFromFile()
            displayCategories(categories)
        } catch (e: IOException) {
            showToast("Error al añadir la categoría al archivo JSON: ${e.message}")
        }
    }
    private fun getCurrentDateAsString(): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return dateFormat.format(getCurrentDate())
    }
    private fun readJSONFileContents(file: File): JSONArray {
        if (file.exists()) {
            try {
                BufferedReader(FileReader(file)).use { reader ->
                    val jsonString = reader.readText()
                    return JSONArray(jsonString)
                }
            } catch (e: Exception) {
                showToast("Error al leer el archivo JSON: ${e.message}")
            }
        }
        return JSONArray()
    }
    private fun writeJSONFileContents(file: File, jsonArray: JSONArray) {
        try {
            FileWriter(file).use { writer ->
                writer.write(jsonArray.toString())
            }
        } catch (e: IOException) {
            showToast("Error al escribir en el archivo JSON: ${e.message}")
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    private fun showMessage(message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(message)
            .setPositiveButton("Aceptar") { _, _ ->
            }
        val dialog = builder.create()
        dialog.show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun deleteSelectedCategories(selectedCategories: Set<Categoria>?) {
        if (selectedCategories != null && selectedCategories.isNotEmpty()) {
            val fileExtension = getFileExtension()
            var fileName = ""
            fileName = if (fileExtension.equals("JSON")) {
                "listaCategorias.json"
            } else if (fileExtension.equals("CSV")) {
                "listaCategorias.csv"
            } else {
                showMessage("Extensión de archivo no válida: $fileExtension")
                return
            }
            val file = File(requireContext().getExternalFilesDir(null), fileName)
            val categories = readCategoriesFromFile()
            val remainingCategories = categories.filter { !selectedCategories.contains(it) }
            updateCategoriesFile(file, remainingCategories)
            displayCategories(remainingCategories)
        } else {
            showToast("No se han seleccionado categorías para eliminar.")
        }
    }
    private fun updateCategoriesFile(file: File, categories: List<Categoria>) {
        try {
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            when (getFileExtension()) {
                "JSON" -> writeCategoriesToJSONFile(file, categories)
                "CSV" -> writeCategoriesToCSVFile(file, categories)
                else -> showMessage("Extensión de archivo no válida: ${getFileExtension()}")
            }
        } catch (e: IOException) {
            showToast("Error al actualizar el archivo de categorías: ${e.message}")
        }
    }
    private fun writeCategoriesToJSONFile(file: File, categories: List<Categoria>) {
        try {
            val jsonArray = JSONArray()
            categories.forEach { category ->
                val jsonObject = JSONObject()
                jsonObject.put("nombre", category.nombre)
                jsonObject.put("fecha", SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(category.fecha))
                jsonArray.put(jsonObject)
            }
            FileWriter(file).use { writer ->
                writer.write(jsonArray.toString())
            }
        } catch (e: IOException) {
            showToast("Error al escribir en el archivo JSON: ${e.message}")
        }
    }
    private fun writeCategoriesToCSVFile(file: File, categories: List<Categoria>) {
        try {
            FileWriter(file).use { writer ->
                categories.forEach { category ->
                    writer.append("${category.nombre},${SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(category.fecha)}\n")
                }
            }
        } catch (e: IOException) {
            showToast("Error al escribir en el archivo CSV: ${e.message}")
        }
    }

}