
# CityPulse-Android
Real-time weather app for Android built with MVVM architecture.

## Features
- Real-time weather with OpenWeather API
- Google Maps with automatic day/night style
- Rain overlay animations
- City search
- Canvas weather animations (sun, clouds, rain, storm, snow)

## Architecture
MVVM + Repository Pattern

## Tech Stack
| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose |
| Networking | Retrofit + Gson |
| Maps | Google Maps SDK |
| DI | Hilt |
| Async | Coroutines + StateFlow |
| Image loading | Coil |
| Package Manager | Gradle (KTS) |

## Architecture Diagram
```mermaid
graph TD
    subgraph View
        DS[DashboardScreen]
        MS[MapScreen]
        WA[WeatherAnimation]
    end
    subgraph ViewModel
        WVM[WeatherViewModel]
        WUS[WeatherUiState]
    end
    subgraph Repository
        WR[WeatherRepository]
    end
    subgraph Data
        API[WeatherApi]
        RI[RetrofitInstance]
        WM[WeatherModel]
    end
    View -- "collectAsState()" --> ViewModel
    ViewModel -- "viewModelScope" --> Repository
    Repository -- "Retrofit" --> Data
    Data -- "REST/JSON" --> External[OpenWeather API]
```