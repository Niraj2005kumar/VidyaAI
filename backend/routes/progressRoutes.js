import express from 'express';
import authMiddleware from '../middleware/authMiddleware.js';
import Progress from '../models/Progress.js';

const router = express.Router();

// Create or update topic progress
router.post('/update', authMiddleware, async (req, res) => {
  try {
    const {
      classLevel,
      subject,
      chapter,
      topic,
      questionsAsked,
      quizzesAttempted,
      averageScore,
      studyTimeMinutes,
      masteryLevel,
      isWeakArea,
    } = req.body;

    if (!classLevel || !subject || !topic) {
      return res.status(400).json({
        success: false,
        message: 'classLevel, subject and topic are required',
      });
    }

    let progress = await Progress.findOne({
      user: req.user.id,
      classLevel,
      subject,
      chapter: chapter || null,
      topic,
    });

    if (progress) {
      progress.questionsAsked = questionsAsked ?? progress.questionsAsked;

      progress.quizzesAttempted = quizzesAttempted ?? progress.quizzesAttempted;

      progress.averageScore = averageScore ?? progress.averageScore;

      progress.studyTimeMinutes = studyTimeMinutes ?? progress.studyTimeMinutes;

      progress.masteryLevel = masteryLevel ?? progress.masteryLevel;

      progress.isWeakArea = isWeakArea ?? progress.isWeakArea;

      progress.lastStudiedAt = new Date();

      await progress.save();
    } else {
      progress = await Progress.create({
        user: req.user.id,
        classLevel,
        subject,
        chapter: chapter || null,
        topic,
        questionsAsked: questionsAsked || 0,
        quizzesAttempted: quizzesAttempted || 0,
        averageScore: averageScore || 0,
        studyTimeMinutes: studyTimeMinutes || 0,
        masteryLevel: masteryLevel || 'beginner',
        isWeakArea: isWeakArea || false,
        lastStudiedAt: new Date(),
      });
    }

    res.status(200).json({
      success: true,
      message: 'Progress updated successfully',
      data: progress,
    });
  } catch (error) {
    console.error('Progress update error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to update progress',
    });
  }
});

// Get all progress
router.get('/', authMiddleware, async (req, res) => {
  try {
    const progress = await Progress.find({
      user: req.user.id,
    }).sort({ lastStudiedAt: -1 });

    res.json({
      success: true,
      count: progress.length,
      data: progress,
    });
  } catch (error) {
    console.error('Get progress error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to fetch progress',
    });
  }
});

// Get weak areas
router.get('/weak-areas', authMiddleware, async (req, res) => {
  try {
    const weakAreas = await Progress.find({
      user: req.user.id,
      isWeakArea: true,
    }).sort({ lastStudiedAt: -1 });

    res.json({
      success: true,
      count: weakAreas.length,
      data: weakAreas,
    });
  } catch (error) {
    console.error('Weak areas error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to fetch weak areas',
    });
  }
});

// Get progress by subject
router.get('/subject/:subject', authMiddleware, async (req, res) => {
  try {
    const progress = await Progress.find({
      user: req.user.id,
      subject: req.params.subject,
    }).sort({ lastStudiedAt: -1 });

    res.json({
      success: true,
      subject: req.params.subject,
      count: progress.length,
      data: progress,
    });
  } catch (error) {
    console.error('Subject progress error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to fetch subject progress',
    });
  }
});

// Overall progress summary
router.get('/summary', authMiddleware, async (req, res) => {
  try {
    const progress = await Progress.find({
      user: req.user.id,
    });

    const totalTopics = progress.length;

    const masteredTopics = progress.filter(
      (item) => item.masteryLevel === 'mastered',
    ).length;

    const weakTopics = progress.filter((item) => item.isWeakArea).length;

    const totalQuestions = progress.reduce(
      (total, item) => total + (item.questionsAsked || 0),
      0,
    );

    const totalQuizzes = progress.reduce(
      (total, item) => total + (item.quizzesAttempted || 0),
      0,
    );

    const totalStudyTime = progress.reduce(
      (total, item) => total + (item.studyTimeMinutes || 0),
      0,
    );

    const averageScore =
      totalTopics > 0
        ? progress.reduce(
            (total, item) => total + (item.averageScore || 0),
            0,
          ) / totalTopics
        : 0;

    res.json({
      success: true,
      data: {
        totalTopics,
        masteredTopics,
        weakTopics,
        totalQuestions,
        totalQuizzes,
        totalStudyTimeMinutes: totalStudyTime,
        averageScore: Number(averageScore.toFixed(2)),
      },
    });
  } catch (error) {
    console.error('Progress summary error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to generate progress summary',
    });
  }
});

export default router;
