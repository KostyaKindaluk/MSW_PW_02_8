package com.example.pw_02_8

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pw_02_8.data.db.BiogasDatabase
import com.example.pw_02_8.repository.BiogasRepository
import com.example.pw_02_8.screens.BiogasScreen
import com.example.pw_02_8.ui.theme.PW_02_8Theme
import com.example.pw_02_8.viewmodel.BiogasViewModel
import com.example.pw_02_8.viewmodel.BiogasViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = BiogasDatabase.getDatabase(applicationContext)
        val repository = BiogasRepository(database.reactorDao(), database.measurementDao())

        setContent {
            PW_02_8Theme {
                val viewModel: BiogasViewModel = viewModel(
                    factory = BiogasViewModelFactory(repository)
                )

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BiogasScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}