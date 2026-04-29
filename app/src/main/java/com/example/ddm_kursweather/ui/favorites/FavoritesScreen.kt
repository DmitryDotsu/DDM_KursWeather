package com.example.ddm_kursweather.ui.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ddm_kursweather.data.local.entity.SavedCity
import com.example.ddm_kursweather.data.repository.SavedCitiesRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    savedCitiesRepository: SavedCitiesRepository,
    refreshTrigger: Int = 0,
    onCityClick: (SavedCity) -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Ключ для принудительного обновления
    val viewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModelFactory(savedCitiesRepository)
    )

    val uiState by viewModel.uiState.collectAsState()

    // Обновляем список
    LaunchedEffect(refreshTrigger) {
        viewModel.loadFavorites()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Избранные города") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is FavoritesUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is FavoritesUiState.Success -> {
                    if (state.cities.isEmpty()) {
                        Text(
                            text = "⭐ Нет сохранённых городов\nВ поиске нажмите «Сохранить в избранное»",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.cities) { city ->
                                FavoriteCityCard(
                                    city = city,
                                    onDelete = {
                                        viewModel.removeCity(city.id)
                                        // Небольшая задержка перед обновлением
                                        viewModel.loadFavorites()
                                    },
                                    onClick = { onCityClick(city) }
                                )
                            }
                        }
                    }
                }
                is FavoritesUiState.Error -> {
                    Text(
                        text = "❌ Ошибка: ${state.message}",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteCityCard(
    city: SavedCity,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = city.fullName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}