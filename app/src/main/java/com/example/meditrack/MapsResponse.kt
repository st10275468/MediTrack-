data class NearbySearchRequest(
    val includedTypes: List<String>,
    val maxResultCount: Int = 20,
    val locationRestriction: LocationRestriction
)

data class LocationRestriction(
    val circle: Circle
)

data class Circle(
    val center: Center,
    val radius: Double
)

data class Center(
    val latitude: Double,
    val longitude: Double
)

data class NearbySearchResponse(
    val places: List<Place>?
)

data class Place(
    val displayName: LocalizedText?,
    val location: Location,
    val types: List<String>?,
    val formattedAddress: String?
)

data class LocalizedText(
    val text: String,
    val languageCode: String?
)

data class Location(
    val latitude: Double,
    val longitude: Double
)