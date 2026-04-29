package com.example.ddm_kursweather.ui.weather

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ddm_kursweather.data.api.RetrofitClient
import com.example.ddm_kursweather.data.local.entity.SavedCity
import com.example.ddm_kursweather.data.models.CurrentWeather
import com.example.ddm_kursweather.data.models.CityInfo
import com.example.ddm_kursweather.data.repository.WeatherRepositoryImpl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    selectedCityFromFavorites: SavedCity? = null,
    onClearSelectedCity: () -> Unit = {},
    onSaveCity: ((CityInfo) -> Unit)? = null,
    onRemoveCity: ((CityInfo) -> Unit)? = null,
    isCitySaved: ((String) -> Boolean)? = null,
    onShowFavorites: (() -> Unit)? = null
) {
    val geocodingApi = RetrofitClient.getGeocodingApi()
    val openMeteoApi = RetrofitClient.getOpenMeteoApi()
    val repository = WeatherRepositoryImpl(geocodingApi, openMeteoApi)

    val viewModel: WeatherViewModel = viewModel(
        factory = WeatherViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var textFieldValue by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }

    //  Bundle для сохранения состояния при повороте и навигации
    val bundle = remember { Bundle() }

    var selectedCityName by remember { mutableStateOf(bundle.getString("selectedCityName") ?: "") }
    var selectedCityFullName by remember { mutableStateOf(bundle.getString("selectedCityFullName") ?: "") }
    var selectedCityLat by remember { mutableStateOf(bundle.getDouble("selectedCityLat", 0.0)) }
    var selectedCityLon by remember { mutableStateOf(bundle.getDouble("selectedCityLon", 0.0)) }
    var isSaved by remember { mutableStateOf(false) }
    var lastWeatherCityName by remember { mutableStateOf("") }

    // Восстанавливаем City из Bundle
    val selectedCity = if (selectedCityName.isNotEmpty()) {
        CityInfo(
            name = selectedCityName,
            fullName = selectedCityFullName,
            latitude = selectedCityLat,
            longitude = selectedCityLon,
            isCapital = false,
            isRegionalCenter = false,
            adminLevelIcon = "📍"
        )
    } else null

    // Сохраняем в Bundle при изменениях
    LaunchedEffect(selectedCityName, selectedCityFullName, selectedCityLat, selectedCityLon) {
        bundle.putString("selectedCityName", selectedCityName)
        bundle.putString("selectedCityFullName", selectedCityFullName)
        bundle.putDouble("selectedCityLat", selectedCityLat)
        bundle.putDouble("selectedCityLon", selectedCityLon)
    }

    // Проверяем, сохранён ли город
    LaunchedEffect(selectedCity) {
        if (selectedCity != null && isCitySaved != null) {
            isSaved = isCitySaved(selectedCity.name)
        }
    }

    // Автозагрузка города из избранного
    LaunchedEffect(selectedCityFromFavorites) {
        if (selectedCityFromFavorites != null) {
            textFieldValue = selectedCityFromFavorites.name
            showSuggestions = false
            selectedCityName = selectedCityFromFavorites.name
            selectedCityFullName = selectedCityFromFavorites.fullName
            selectedCityLat = selectedCityFromFavorites.latitude
            selectedCityLon = selectedCityFromFavorites.longitude
            val cityInfo = CityInfo(
                name = selectedCityFromFavorites.name,
                fullName = selectedCityFromFavorites.fullName,
                latitude = selectedCityFromFavorites.latitude,
                longitude = selectedCityFromFavorites.longitude,
                isCapital = false,
                isRegionalCenter = false,
                adminLevelIcon = "📍"
            )
            viewModel.loadWeather(cityInfo)
            onClearSelectedCity()
        }
    }

    // При успешной загрузке погоды сохраняем название города
    LaunchedEffect(uiState) {
        if (uiState is WeatherUiState.Success && lastWeatherCityName.isEmpty()) {
            val successState = uiState as WeatherUiState.Success
            if (selectedCityName.isEmpty()) {
                selectedCityName = successState.cityName
                selectedCityFullName = successState.cityName
            }
            lastWeatherCityName = successState.cityName
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поиск погоды") },
                actions = {
                    IconButton(onClick = { onShowFavorites?.invoke() }) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Избранное"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    viewModel.searchCity(it)
                    showSuggestions = it.isNotEmpty()
                },
                label = { Text("Название города") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (showSuggestions && searchResults.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn {
                        items(searchResults) { city ->
                            TextButton(
                                onClick = {
                                    textFieldValue = city.name
                                    showSuggestions = false
                                    selectedCityName = city.name
                                    selectedCityFullName = city.fullName
                                    selectedCityLat = city.latitude
                                    selectedCityLon = city.longitude
                                    viewModel.loadWeather(city)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = city.adminLevelIcon,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (city.isCapital) "⭐ " else "  ",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = city.name,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    Text(
                                        text = city.fullName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
                    Column {
                        WeatherContent(
                            cityName = state.cityName,
                            weather = state.weather
                        )

                        if (onSaveCity != null && selectedCity != null) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (isSaved) {
                                        onRemoveCity?.invoke(selectedCity)
                                        isSaved = false
                                    } else {
                                        onSaveCity(selectedCity)
                                        isSaved = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSaved)
                                        MaterialTheme.colorScheme.secondaryContainer
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (isSaved) "В избранном" else "Сохранить в избранное"
                                )
                            }
                        }
                    }
                }
                is WeatherUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "❌ ${state.message}",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                WeatherUiState.Initial -> {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🌍 Введите название города",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
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