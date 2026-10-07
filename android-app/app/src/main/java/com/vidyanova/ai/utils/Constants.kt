package com.vidyanova.ai.utils

object Constants {

    const val BASE_URL = "http://10.0.2.2:5000/"

    const val HEALTH_ENDPOINT = "api/health"

    const val LOGIN_ENDPOINT = "api/auth/login"
    const val REGISTER_ENDPOINT = "api/auth/register"

    const val USER_PROFILE_ENDPOINT = "api/users/profile"

    const val STUDY_START_ENDPOINT = "api/study/start"
    const val STUDY_END_ENDPOINT = "api/study/end/{id}"
    const val STUDY_HISTORY_ENDPOINT = "api/study/history"
    const val STUDY_TOTAL_TIME_ENDPOINT = "api/study/total-time"

    const val PROGRESS_ENDPOINT = "api/progress"
    const val PROGRESS_UPDATE_ENDPOINT = "api/progress/update"
    const val PROGRESS_SUMMARY_ENDPOINT = "api/progress/summary"
    const val WEAK_AREAS_ENDPOINT = "api/progress/weak-areas"

    const val QUIZ_ENDPOINT = "api/quizzes"
    const val QUIZ_ATTEMPTS_ENDPOINT = "api/quizzes/attempts/my"

    const val OPPORTUNITIES_ENDPOINT = "api/opportunities"

    const val TUTOR_ENDPOINT = "api/tutor/question"
}