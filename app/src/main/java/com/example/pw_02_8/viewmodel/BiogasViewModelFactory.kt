package com.example.pw_02_8.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pw_02_8.repository.BiogasRepository

class BiogasViewModelFactory(private val repository: BiogasRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BiogasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BiogasViewModel(repository) as T
        }
        throw IllegalArgumentException("Невідомий клас ViewModel")
    }
}