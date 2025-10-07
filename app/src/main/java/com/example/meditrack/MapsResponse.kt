/**
 * MapsResponse.kt
 *
 * Data models to parse response from Google maps and Geoapify APIs
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */
data class GeoapifyPlacesResponse(
    val features: List<Feature>
)

data class Feature(
    val geometry: Geometry,
    val properties: Properties
)

data class Geometry(
    val coordinates: List<Double>
)

data class Properties(
    val name: String?,
    val categories: List<String>?
)
