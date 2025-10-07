import com.example.meditrack.MedicineApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Retrofit instance for use with FDA API
object RetrofitInstance {
    val api: MedicineApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.fda.gov/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MedicineApi::class.java)
    }
}
