package com.a23izadelesp.quizzapp

import QuizViewModel
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.a23izadelesp.quizzapp.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: QuizViewModel
    private var timer: CountDownTimer? = null
    private var timeElapsed: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(QuizViewModel::class.java)

        setupObservers()
        startTimer()
        viewModel.loadQuestions()
    }

    private fun setupObservers() {
        viewModel.quizState.observe(this) { state ->
            when (state) {
                is QuizState.Loading -> showLoading()
                is QuizState.Question -> showQuestion()
                is QuizState.Result -> showResult(state.aciertos)
                is QuizState.Error -> showError(state.message)
                else -> {}
            }
        }

        viewModel.currentQuestion.observe(this) { question ->
            binding.questionTextView.text = question.pregunta
            binding.answersContainer.removeAllViews()
            question.respostes.forEach { resposta ->
                val answerLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, 32) // Increased margin between answers
                    }
                }

                val imageView = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        500 // Increased height for better visibility
                    )
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    adjustViewBounds = true // This allows the image to adjust its bounds
                }

                Glide.with(this)
                    .load(resposta.imatge)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .fitCenter()
                    .into(imageView)

                val button = Button(this).apply {
                    text = resposta.resposta
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setOnClickListener {
                        viewModel.submitAnswer(resposta.resposta)
                    }
                }

                answerLayout.addView(imageView)
                answerLayout.addView(button)
                binding.answersContainer.addView(answerLayout)
            }
        }
    }

    private fun showLoading() {
        binding.loadingView.visibility = View.VISIBLE
        binding.questionTextView.visibility = View.GONE
        binding.answersContainer.visibility = View.GONE
    }

    private fun showQuestion() {
        binding.loadingView.visibility = View.GONE
        binding.questionTextView.visibility = View.VISIBLE
        binding.answersContainer.visibility = View.VISIBLE
    }

    private fun showResult(aciertos: Int) {
        timer?.cancel()
        val intent = Intent(this, FinalActivity::class.java)
        intent.putExtra("SCORE", aciertos)
        intent.putExtra("TIME", formatTime(timeElapsed))
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        // Mostrar un diálogo de error o un Toast con el mensaje
    }

    private fun startTimer() {
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeElapsed += 1000
                binding.timerTextView.text = formatTime(timeElapsed)
            }
            override fun onFinish() {}
        }.start()
    }

    private fun formatTime(millis: Long): String {
        val seconds = millis / 1000
        return String.format("%02d:%02d", seconds / 60, seconds % 60)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}