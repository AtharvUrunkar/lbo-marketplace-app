import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.lbo_marketplace.auth.FirebaseAuthManager
class GreetingViewModel : ViewModel() {

    private val apiClient = ApiClient()

    private val _uiState = MutableStateFlow("Loading...")
    val uiState: StateFlow<String> = _uiState

    init {
        fetch()
    }

    private fun fetch() {
        viewModelScope.launch {
            try {
                _uiState.value = apiClient.testCall()
            } catch (e: Exception) {
                _uiState.value = "Error: ${e.message}"
            }
        }
    }
}
