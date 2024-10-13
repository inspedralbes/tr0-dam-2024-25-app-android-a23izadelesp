package com.a23izadelesp.quizzapp

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface QuizApiService {
    @GET("getPreguntas")
    suspend fun getQuestions(): List<Question>

    @POST("finalista")
    suspend fun submitAnswers(@Body submission: AnswerSubmission): Result
}