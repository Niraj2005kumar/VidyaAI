import express from 'express';
import Quiz from '../models/Quiz.js';
import QuizAttempt from '../models/QuizAttempt.js';
import authMiddleware from '../middleware/authMiddleware.js';

const router = express.Router();

/*
  POST /api/quizzes/
  Create a quiz
*/
router.post('/', authMiddleware, async (req, res) => {
  try {
    const { title, classLevel, subject, chapter, questions } = req.body;

    if (!title || !classLevel || !subject || !questions?.length) {
      return res.status(400).json({
        success: false,
        message: 'Title, class level, subject and questions are required',
      });
    }

    const quiz = await Quiz.create({
      title: title.trim(),
      classLevel: Number(classLevel),
      subject: subject.trim(),
      chapter: chapter?.trim() || '',
      questions,
      createdBy: req.user.id,
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

/*
  GET /api/quizzes/
  Get quizzes
*/
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

    res.status(200).json({
      success: true,
      count: quizzes.length,
      quizzes,
    });
  } catch (error) {
    console.error('Get quizzes error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to fetch quizzes',
    });
  }
});

/*
  GET /api/quizzes/:id
  Get one quiz
*/
router.get('/:id', authMiddleware, async (req, res) => {
  try {
    const quiz = await Quiz.findById(req.params.id);

    if (!quiz) {
      return res.status(404).json({
        success: false,
        message: 'Quiz not found',
      });
    }

    res.status(200).json({
      success: true,
      quiz,
    });
  } catch (error) {
    console.error('Get quiz error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to fetch quiz',
    });
  }
});

/*
  POST /api/quizzes/:id/submit
  Submit quiz
*/
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

    const evaluatedAnswers = quiz.questions.map((question, index) => {
      const submitted = answers[index];

      const selectedAnswer = submitted?.selectedAnswer ?? submitted ?? null;

      const correct = String(selectedAnswer) === String(question.correctAnswer);

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
      result: {
        attemptId: attempt._id,
        score: correctAnswers,
        totalQuestions,
        correctAnswers,
        percentage,
        answers: evaluatedAnswers.map((answer, index) => ({
          ...answer,
          explanation: quiz.questions[index]?.explanation || '',
        })),
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

/*
  GET /api/quizzes/attempts/my
  Get logged-in user's quiz attempts
*/
router.get('/attempts/my', authMiddleware, async (req, res) => {
  try {
    const attempts = await QuizAttempt.find({
      user: req.user.id,
    })
      .populate('quiz', 'title classLevel subject chapter')
      .sort({ completedAt: -1 })
      .limit(100);

    res.status(200).json({
      success: true,
      count: attempts.length,
      attempts,
    });
  } catch (error) {
    console.error('Get quiz attempts error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to fetch quiz attempts',
    });
  }
});

export default router;
