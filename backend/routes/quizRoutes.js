import express from 'express';
import Quiz from '../models/Quiz.js';
import QuizAttempt from '../models/QuizAttempt.js';
import authMiddleware from '../middleware/authMiddleware.js';

const router = express.Router();

// ======================================
// CREATE QUIZ
// ======================================
router.post('/', authMiddleware, async (req, res) => {
  try {
    const { title, classLevel, subject, chapter, questions } = req.body;

    if (
      !title ||
      !classLevel ||
      !subject ||
      !questions ||
      !Array.isArray(questions) ||
      questions.length === 0
    ) {
      return res.status(400).json({
        success: false,
        message: 'Title, class, subject and questions are required',
      });
    }

    const quiz = await Quiz.create({
      title,
      classLevel,
      subject,
      chapter,
      questions,
      createdBy: req.user.userId,
    });

    res.status(201).json({
      success: true,
      message: 'Quiz created successfully',
      quiz,
    });
  } catch (error) {
    console.error('Create quiz error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to create quiz',
    });
  }
});

// ======================================
// GET ALL QUIZZES
// ======================================
router.get('/', authMiddleware, async (req, res) => {
  try {
    const { classLevel, subject, chapter } = req.query;

    const filter = {};

    if (classLevel) {
      filter.classLevel = Number(classLevel);
    }

    if (subject) {
      filter.subject = subject;
    }

    if (chapter) {
      filter.chapter = chapter;
    }

    const quizzes = await Quiz.find(filter)
      .select('-questions.correctAnswer')
      .sort({
        createdAt: -1,
      });

    res.status(200).json({
      success: true,
      count: quizzes.length,
      quizzes,
    });
  } catch (error) {
    console.error('Get quizzes error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to get quizzes',
    });
  }
});

// ======================================
// GET QUIZ BY ID
// ======================================
router.get('/:quizId', authMiddleware, async (req, res) => {
  try {
    const quiz = await Quiz.findById(req.params.quizId);

    if (!quiz) {
      return res.status(404).json({
        success: false,
        message: 'Quiz not found',
      });
    }

    // Do not send correct answers before submission
    const safeQuestions = quiz.questions.map((question) => ({
      _id: question._id,
      question: question.question,
      options: question.options,
      explanation: question.explanation,
      difficulty: question.difficulty,
    }));

    res.status(200).json({
      success: true,
      quiz: {
        id: quiz._id,
        title: quiz.title,
        classLevel: quiz.classLevel,
        subject: quiz.subject,
        chapter: quiz.chapter,
        questions: safeQuestions,
      },
    });
  } catch (error) {
    console.error('Get quiz error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to get quiz',
    });
  }
});

// ======================================
// SUBMIT QUIZ ATTEMPT
// ======================================
router.post('/:quizId/attempt', authMiddleware, async (req, res) => {
  try {
    const { answers } = req.body;

    if (!Array.isArray(answers)) {
      return res.status(400).json({
        success: false,
        message: 'Answers array is required',
      });
    }

    const quiz = await Quiz.findById(req.params.quizId);

    if (!quiz) {
      return res.status(404).json({
        success: false,
        message: 'Quiz not found',
      });
    }

    let correctAnswers = 0;

    const evaluatedAnswers = answers.map((answer) => {
      const question = quiz.questions.id(answer.questionId);

      if (!question) {
        return {
          questionId: answer.questionId,
          selectedAnswer: answer.selectedAnswer,
          correct: false,
        };
      }

      const isCorrect = question.correctAnswer === answer.selectedAnswer;

      if (isCorrect) {
        correctAnswers++;
      }

      return {
        questionId: answer.questionId,
        selectedAnswer: answer.selectedAnswer,
        correct: isCorrect,
      };
    });

    const totalQuestions = quiz.questions.length;

    const score = correctAnswers;

    const percentage =
      totalQuestions > 0
        ? Number(((correctAnswers / totalQuestions) * 100).toFixed(2))
        : 0;

    const attempt = await QuizAttempt.create({
      user: req.user.userId,
      quiz: quiz._id,
      answers: evaluatedAnswers,
      score,
      totalQuestions,
      correctAnswers,
      percentage,
      completedAt: new Date(),
    });

    res.status(201).json({
      success: true,
      message: 'Quiz submitted successfully',

      result: {
        attemptId: attempt._id,
        score,
        totalQuestions,
        correctAnswers,
        percentage,
      },
    });
  } catch (error) {
    console.error('Submit quiz error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to submit quiz',
    });
  }
});

// ======================================
// GET USER QUIZ ATTEMPTS
// ======================================
router.get('/attempts/my', authMiddleware, async (req, res) => {
  try {
    const attempts = await QuizAttempt.find({
      user: req.user.userId,
    })
      .populate('quiz', 'title classLevel subject chapter')
      .sort({
        createdAt: -1,
      });

    res.status(200).json({
      success: true,
      count: attempts.length,
      attempts,
    });
  } catch (error) {
    console.error('Get quiz attempts error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to get quiz attempts',
    });
  }
});

export default router;
