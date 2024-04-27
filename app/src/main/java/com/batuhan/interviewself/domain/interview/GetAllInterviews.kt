package com.batuhan.interviewself.domain.interview

import com.batuhan.interviewself.data.repository.InterviewRepository
import javax.inject.Inject

class GetAllInterviews @Inject constructor(private val repository: InterviewRepository) {

    operator fun invoke() = repository.getAllInterviews()
}