
import com.example.meditrack.MapsApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitMapsInstance {
    val api: MapsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.geoapify.com/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MapsApi::class.java)
    }
}