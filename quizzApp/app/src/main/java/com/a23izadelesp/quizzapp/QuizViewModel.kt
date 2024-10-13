import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a23izadelesp.quizzapp.QuizApiService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.a23izadelesp.quizzapp.*

class QuizViewModel : ViewModel() {
    private val _quizState = MutableLiveData<QuizState>()
    val quizState: LiveData<QuizState> = _quizState

    private val _currentQuestion = MutableLiveData<Question>()
    val currentQuestion: LiveData<Question> = _currentQuestion

    private val questions = mutableListOf<Question>()
    private var currentIndex = -1 // Cambiamos esto a -1
    private val userAnswers = mutableListOf<Answer>()

    private val apiService = Retrofit.Builder()
        .baseUrl("http://a23izadelesp.dam.inspedralbes.cat:20007/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(QuizApiService::class.java)

    fun loadQuestions() {
        viewModelScope.launch {
            try {
                Log.d("QuizViewModel", "Iniciando carga de preguntas")
                _quizState.value = QuizState.Loading
                val response = apiService.getQuestions()
                Log.d("QuizViewModel", "Respuesta recibida: ${response.size} preguntas")
                if (response.isNotEmpty()) {
                    questions.addAll(response)
                    nextQuestion() // Esto ahora mostrará la primera pregunta
                } else {
                    _quizState.value = QuizState.Error("No se encontraron preguntas")
                }
            } catch (e: Exception) {
                Log.e("QuizViewModel", "Error loading questions", e)
                _quizState.value = QuizState.Error("Error al cargar las preguntas: ${e.message}")
            }
        }
    }

    fun submitAnswer(answer: String) {
        userAnswers.add(Answer(currentIndex, answer))
        nextQuestion()
    }

    private fun nextQuestion() {
        currentIndex++
        if (currentIndex < questions.size) {
            _currentQuestion.value = questions[currentIndex]
            _quizState.value = QuizState.Question
        } else {
            submitAllAnswers()
        }
    }

    private fun submitAllAnswers() {
        viewModelScope.launch {
            try {
                _quizState.value = QuizState.Submitting
                val submission = AnswerSubmission(userAnswers)
                Log.d("QuizViewModel", "Enviando respuestas: $submission")
                val result = apiService.submitAnswers(submission)
                Log.d("QuizViewModel", "Respuesta del servidor: $result")
                _quizState.value = QuizState.Result(result.aciertos)
            } catch (e: Exception) {
                Log.e("QuizViewModel", "Error submitting answers", e)
                _quizState.value = QuizState.Error("Error al enviar las respuestas: ${e.message}")
            }
        }
    }
}

sealed class QuizState {
    object Loading : QuizState()
    object Question : QuizState()
    object Submitting : QuizState()
    data class Result(val aciertos: Int) : QuizState()
    data class Error(val message: String) : QuizState()
}