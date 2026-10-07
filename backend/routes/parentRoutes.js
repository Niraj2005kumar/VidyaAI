import express from 'express';

import authMiddleware from '../middleware/authMiddleware.js';
import StudySession from '../models/StudySession.js';
import QuizAttempt from '../models/QuizAttempt.js';
import Progress from '../models/Progress.js';

const router = express.Router();

/*
    GET /api/parent/child/:userId/overview

    Parent dashboard overview:
    - Total study time
    - Quiz performance
    - Weak areas
    - Recent study activity
*/

router.get('/child/:userId/overview', authMiddleware, async (req, res) => {
  try {
    const { userId } = req.params;

    // Total study time
    const studySessions = await StudySession.find({
      user: userId,
    }).sort({ createdAt: -1 });

    const totalStudyTime = studySessions.reduce((total, session) => {
      return total + (session.durationMinutes || 0);
    }, 0);

    // Quiz attempts
    const quizAttempts = await QuizAttempt.find({
      user: userId,
    }).sort({ completedAt: -1 });

    const totalQuizzes = quizAttempts.length;

    const averageScore =
      totalQuizzes > 0
        ? quizAttempts.reduce(
            (total, attempt) => total + (attempt.percentage || 0),
            0,
          ) / totalQuizzes
        : 0;

    // Weak areas
    const weakAreas = await Progress.find({
      user: userId,
      isWeakArea: true,
    }).sort({ lastStudiedAt: -1 });

    res.json({
      success: true,
      data: {
        totalStudyTimeMinutes: totalStudyTime,
        totalQuizzes,
        averageQuizScore: Number(averageScore.toFixed(2)),
        weakAreas,
        recentStudySessions: studySessions.slice(0, 10),
        recentQuizAttempts: quizAttempts.slice(0, 10),
      },
    });
  } catch (error) {
    console.error('Parent dashboard error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to load parent dashboard',
    });
  }
});

/*
    GET /api/parent/child/:userId/study-summary

    Study time summary for parent.
*/

router.get('/child/:userId/study-summary', authMiddleware, async (req, res) => {
  try {
    const { userId } = req.params;

    const sessions = await StudySession.find({
      user: userId,
    }).sort({ startTime: -1 });

    const summary = sessions.map((session) => ({
      subject: session.subject,
      topic: session.topic,
      durationMinutes: session.durationMinutes,
      startTime: session.startTime,
      endTime: session.endTime,
    }));

    res.json({
      success: true,
      count: summary.length,
      data: summary,
    });
  } catch (error) {
    console.error('Study summary error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to load study summary',
    });
  }
});

/*
    GET /api/parent/child/:userId/progress

    Academic progress for parent.
*/

router.get('/child/:userId/progress', authMiddleware, async (req, res) => {
  try {
    const { userId } = req.params;

    const progress = await Progress.find({
      user: userId,
    }).sort({
      lastStudiedAt: -1,
    });

    res.json({
      success: true,
      count: progress.length,
      data: progress,
    });
  } catch (error) {
    console.error('Parent progress error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to load student progress',
    });
  }
});

export default router;
