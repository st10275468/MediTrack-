data class GeoapifyPlacesResponse(
    val features: List<Feature>
)

data class Feature(
    val geometry: Geometry,
    val properties: Properties
)

data class Geometry(
    val coordinates: List<Double> // [lon, lat]
)

data class Properties(
    val name: String?,
    val categories: List<String>?
)
