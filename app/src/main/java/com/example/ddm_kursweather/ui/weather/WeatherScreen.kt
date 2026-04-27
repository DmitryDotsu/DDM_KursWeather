package com.example.ddm_kursweather.ui.weather

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ddm_kursweather.data.api.RetrofitClient
import com.example.ddm_kursweather.data.models.CurrentWeather
import com.example.ddm_kursweather.data.repository.WeatherRepositoryImpl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen() {
    val apiKey = "3ZiiAKlxu3NYE8MT0jMx4NkOduayrgF2rzJSeqXg"

    var debugMessage by remember { mutableStateOf("") }

    val ninjasApi = RetrofitClient.getNinjasApi(apiKey)
    val openMeteoApi = RetrofitClient.getOpenMeteoApi()
    val repository = WeatherRepositoryImpl(ninjasApi, openMeteoApi)

    val viewModel: WeatherViewModel = viewModel(
        factory = WeatherViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var textFieldValue by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                debugMessage = "Поиск: $it"
                viewModel.searchCity(it)
                showSuggestions = it.isNotEmpty()
            },
            label = { Text("Название города") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (debugMessage.isNotEmpty()) {
            Text(
                text = debugMessage,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (showSuggestions && searchResults.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn {
                    items(searchResults) { city ->
                        TextButton(
                            onClick = {
                                textFieldValue = city
                                showSuggestions = false
                                debugMessage = "Загрузка погоды для: $city"
                                viewModel.loadWeather(city)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(city, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is WeatherUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is WeatherUiState.Success -> {
                WeatherContent(
                    cityName = state.cityName,
                    weather = state.weather
                )
            }
            is WeatherUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "❌ Ошибка: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Проверьте интернет и API ключ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            WeatherUiState.Initial -> {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🌤️ Введите название города для поиска погоды",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherContent(cityName: String, weather: CurrentWeather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = getWeatherIcon(weather.weathercode),
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = cityName,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${weather.temperature.toInt()}°C",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💨", style = MaterialTheme.typography.titleLarge)
                    Text("${weather.windspeed.toInt()} км/ч", style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🧭", style = MaterialTheme.typography.titleLarge)
                    Text("${weather.winddirection.toInt()}°", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = weather.time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun getWeatherIcon(code: Int): String {
    return when (code) {
        0 -> "☀️"
        1, 2, 3 -> "⛅"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌧️"
        61, 63, 65 -> "🌧️"
        71, 73, 75 -> "❄️"
        80, 81, 82 -> "🌧️"
        else -> "🌡️"
    }
}