import com.example.paperkart.data.dto.user.UserDto

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)