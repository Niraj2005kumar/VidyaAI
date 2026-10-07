import express from 'express';
import authMiddleware from '../middleware/authMiddleware.js';
import Quiz from '../models/Quiz.js';
import QuizAttempt from '../models/QuizAttempt.js';

const router = express.Router();

// Create a quiz
router.post('/', authMiddleware, async (req, res) => {
  try {
    const { title, classLevel, subject, chapter, questions } = req.body;

    if (!title || !classLevel || !subject || !questions?.length) {
      return res.status(400).json({
        success: false,
        message: 'title, classLevel, subject and questions are required',
      });
    }

    const quiz = await Quiz.create({
      title,
      classLevel,
      subject,
      chapter: chapter || null,
      questions,
      createdBy: req.user.id,
    });

    res.status(201).json({
      success: true,
      message: 'Quiz created successfully',
      data: quiz,
    });
  } catch (error) {
    console.error('Create quiz error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to create quiz',
    });
  }
});

// Get all quizzes
router.get('/', authMiddleware, async (req, res) => {
  try {
    const filter = {};

    if (req.query.classLevel) {
      filter.classLevel = Number(req.query.classLevel);
    }

    if (req.query.subject) {
      filter.subject = req.query.subject;
    }

    if (req.query.chapter) {
      filter.chapter = req.query.chapter;
    }

    const quizzes = await Quiz.find(filter)
      .select('-questions.correctAnswer')
      .sort({ createdAt: -1 });

    res.json({
      success: true,
      count: quizzes.length,
      data: quizzes,
    });
  } catch (error) {
    console.error('Get quizzes error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to fetch quizzes',
    });
  }
});

// Get one quiz
router.get('/:id', authMiddleware, async (req, res) => {
  try {
    const quiz = await Quiz.findById(req.params.id);

    if (!quiz) {
      return res.status(404).json({
        success: false,
        message: 'Quiz not found',
      });
    }

    res.json({
      success: true,
      data: quiz,
    });
  } catch (error) {
    console.error('Get quiz error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to fetch quiz',
    });
  }
});

// Submit quiz attempt
router.post('/:id/submit', authMiddleware, async (req, res) => {
  try {
    const { answers } = req.body;

    if (!Array.isArray(answers)) {
      return res.status(400).json({
        success: false,
        message: 'Answers must be an array',
      });
    }

    const quiz = await Quiz.findById(req.params.id);

    if (!quiz) {
      return res.status(404).json({
        success: false,
        message: 'Quiz not found',
      });
    }

    let correctAnswers = 0;

    const evaluatedAnswers = quiz.questions.map((question) => {
      const submitted = answers.find(
        (answer) => String(answer.questionId) === String(question._id),
      );

      const selectedAnswer = submitted ? submitted.selectedAnswer : null;

      const correct =
        selectedAnswer !== null &&
        String(selectedAnswer) === String(question.correctAnswer);

      if (correct) {
        correctAnswers++;
      }

      return {
        questionId: question._id,
        selectedAnswer,
        correct,
      };
    });

    const totalQuestions = quiz.questions.length;

    const percentage =
      totalQuestions > 0
        ? Number(((correctAnswers / totalQuestions) * 100).toFixed(2))
        : 0;

    const attempt = await QuizAttempt.create({
      user: req.user.id,
      quiz: quiz._id,
      answers: evaluatedAnswers,
      score: correctAnswers,
      totalQuestions,
      correctAnswers,
      percentage,
      completedAt: new Date(),
    });

    res.status(201).json({
      success: true,
      message: 'Quiz submitted successfully',
      data: {
        attemptId: attempt._id,
        score: correctAnswers,
        totalQuestions,
        correctAnswers,
        percentage,
        answers: evaluatedAnswers,
      },
    });
  } catch (error) {
    console.error('Submit quiz error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to submit quiz',
    });
  }
});

// Get user's quiz attempts
router.get('/attempts/my', authMiddleware, async (req, res) => {
  try {
    const attempts = await QuizAttempt.find({
      user: req.user.id,
    })
      .populate('quiz', 'title classLevel subject chapter')
      .sort({ completedAt: -1 });

    res.json({
      success: true,
      count: attempts.length,
      data: attempts,
    });
  } catch (error) {
    console.error('Get attempts error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to fetch quiz attempts',
    });
  }
});

export default router;
