package com.a23izadelesp.quizzapp

data class Question(val pregunta: String, val respostes: List<Resposta>)
data class Resposta(val resposta: String, val imatge: String)
data class Answer(val preguntaId: Int, val respuesta: String)
data class Result(val aciertos: Int)
data class AnswerSubmission(val respuestas: List<Answer>)