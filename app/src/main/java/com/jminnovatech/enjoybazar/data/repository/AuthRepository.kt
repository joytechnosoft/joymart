import com.google.gson.Gson
import com.jminnovatech.joymart.data.remote.api.RetrofitClient
import com.jminnovatech.joymart.data.model.auth.LoginRequest
import com.jminnovatech.joymart.data.remote.model.LoginResponse
import retrofit2.HttpException

class AuthRepository {

    suspend fun login(
        mobile: String,
        password: String
    ): Result<LoginResponse> {
        return try {
            val response = RetrofitClient.authApi.login(
                LoginRequest(
                    login = mobile,
                    password = password
                )
            )
            Result.success(response)

        } catch (e: HttpException) {

            val errorJson = e.response()?.errorBody()?.string()
            val apiError = try {
                Gson().fromJson(errorJson, ApiError::class.java)
            } catch (ex: Exception) {
                null
            }

            Result.failure(
                Exception(apiError?.message ?: "Invalid mobile or password")
            )

        } catch (e: Exception) {
            Result.failure(Exception("Network error. Please try again"))
        }
    }
}
