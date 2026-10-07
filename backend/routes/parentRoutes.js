import express from 'express';
import User from '../models/User.js';
import StudySession from '../models/StudySession.js';
import QuizAttempt from '../models/QuizAttempt.js';
import Progress from '../models/Progress.js';
import authMiddleware from '../middleware/authMiddleware.js';

const router = express.Router();

router.get('/child/:userId/overview', authMiddleware, async (req, res) => {
  try {
    const { userId } = req.params;

    const child = await User.findById(userId).select(
      'name email role classLevel preferredLanguage',
    );

    if (!child) {
      return res.status(404).json({
        success: false,
        message: 'Student not found',
      });
    }

    const [progress, studySessions, quizAttempts] = await Promise.all([
      Progress.find({ user: userId }).sort({ lastStudiedAt: -1 }),
      StudySession.find({ user: userId }).sort({ startTime: -1 }).limit(100),
      QuizAttempt.find({ user: userId }).sort({ completedAt: -1 }).limit(100),
    ]);

    const totalStudyTimeMinutes = studySessions.reduce(
      (total, session) => total + (session.durationMinutes || 0),
      0,
    );

    const totalQuizzes = quizAttempts.length;

    const averageQuizScore =
      totalQuizzes > 0
        ? Number(
            (
              quizAttempts.reduce(
                (total, attempt) => total + (attempt.percentage || 0),
                0,
              ) / totalQuizzes
            ).toFixed(2),
          )
        : 0;

    const weakAreas = progress.filter((item) => item.isWeakArea === true);

    const masteredTopics = progress.filter(
      (item) => item.masteryLevel === 'mastered',
    );

    res.status(200).json({
      success: true,
      student: child,
      summary: {
        totalStudyTimeMinutes,
        totalQuizzes,
        averageQuizScore,
        totalTopics: progress.length,
        masteredTopics: masteredTopics.length,
        weakAreas: weakAreas.length,
      },
      weakAreas,
      recentStudySessions: studySessions.slice(0, 10),
      recentQuizAttempts: quizAttempts.slice(0, 10),
    });
  } catch (error) {
    console.error('Parent overview error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to fetch student overview',
    });
  }
});

router.get('/child/:userId/study-summary', authMiddleware, async (req, res) => {
  try {
    const { userId } = req.params;

    const sessions = await StudySession.find({
      user: userId,
    }).sort({
      startTime: -1,
    });

    const totalMinutes = sessions.reduce(
      (total, session) => total + (session.durationMinutes || 0),
      0,
    );

    const subjectTime = {};

    for (const session of sessions) {
      const subject = session.subject || 'Unknown';

      subjectTime[subject] =
        (subjectTime[subject] || 0) + (session.durationMinutes || 0);
    }

    res.status(200).json({
      success: true,
      totalStudyTimeMinutes: totalMinutes,
      totalStudyTimeHours: Number((totalMinutes / 60).toFixed(2)),
      subjectTime,
      sessions,
    });
  } catch (error) {
    console.error('Parent study summary error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to fetch study summary',
    });
  }
});

router.get('/child/:userId/progress', authMiddleware, async (req, res) => {
  try {
    const { userId } = req.params;

    const progress = await Progress.find({
      user: userId,
    }).sort({
      lastStudiedAt: -1,
    });

    const summary = {
      totalTopics: progress.length,
      mastered: 0,
      good: 0,
      learning: 0,
      beginner: 0,
      notStarted: 0,
      weakAreas: 0,
    };

    for (const item of progress) {
      if (item.masteryLevel === 'mastered') {
        summary.mastered++;
      } else if (item.masteryLevel === 'good') {
        summary.good++;
      } else if (item.masteryLevel === 'learning') {
        summary.learning++;
      } else if (item.masteryLevel === 'beginner') {
        summary.beginner++;
      } else {
        summary.notStarted++;
      }

      if (item.isWeakArea) {
        summary.weakAreas++;
      }
    }

    res.status(200).json({
      success: true,
      summary,
      progress,
    });
  } catch (error) {
    console.error('Parent progress error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to fetch student progress',
    });
  }
});

export default router;
