import express from 'express';
import Progress from '../models/Progress.js';
import authMiddleware from '../middleware/authMiddleware.js';

const router = express.Router();

// ======================================
// CREATE / UPDATE TOPIC PROGRESS
// ======================================
router.post('/', authMiddleware, async (req, res) => {
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
        message: 'Class, subject and topic are required',
      });
    }

    const progress = await Progress.findOneAndUpdate(
      {
        user: req.user.userId,
        classLevel,
        subject,
        topic,
      },
      {
        $set: {
          chapter,
          questionsAsked,
          quizzesAttempted,
          averageScore,
          studyTimeMinutes,
          masteryLevel,
          isWeakArea,
          lastStudiedAt: new Date(),
        },
      },
      {
        new: true,
        upsert: true,
        runValidators: true,
      },
    );

    res.status(200).json({
      success: true,
      message: 'Progress updated successfully',
      progress,
    });
  } catch (error) {
    console.error('Update progress error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to update progress',
    });
  }
});

// ======================================
// GET ALL USER PROGRESS
// ======================================
router.get('/', authMiddleware, async (req, res) => {
  try {
    const progress = await Progress.find({
      user: req.user.userId,
    }).sort({
      lastStudiedAt: -1,
    });

    res.status(200).json({
      success: true,
      count: progress.length,
      progress,
    });
  } catch (error) {
    console.error('Get progress error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to get progress',
    });
  }
});

// ======================================
// GET WEAK AREAS
// ======================================
router.get('/weak-areas', authMiddleware, async (req, res) => {
  try {
    const weakAreas = await Progress.find({
      user: req.user.userId,
      isWeakArea: true,
    }).sort({
      averageScore: 1,
    });

    res.status(200).json({
      success: true,
      count: weakAreas.length,
      weakAreas,
    });
  } catch (error) {
    console.error('Get weak areas error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to get weak areas',
    });
  }
});

// ======================================
// GET SUBJECT PROGRESS
// ======================================
router.get('/subject/:subject', authMiddleware, async (req, res) => {
  try {
    const progress = await Progress.find({
      user: req.user.userId,
      subject: req.params.subject,
    }).sort({
      lastStudiedAt: -1,
    });

    res.status(200).json({
      success: true,
      subject: req.params.subject,
      count: progress.length,
      progress,
    });
  } catch (error) {
    console.error('Get subject progress error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to get subject progress',
    });
  }
});

// ======================================
// GET PROGRESS SUMMARY
// ======================================
router.get('/summary', authMiddleware, async (req, res) => {
  try {
    const progress = await Progress.find({
      user: req.user.userId,
    });

    const totalTopics = progress.length;

    const masteredTopics = progress.filter(
      (item) => item.masteryLevel === 'mastered',
    ).length;

    const weakTopics = progress.filter(
      (item) => item.isWeakArea === true,
    ).length;

    const totalQuestions = progress.reduce(
      (total, item) => total + item.questionsAsked,
      0,
    );

    const totalQuizAttempts = progress.reduce(
      (total, item) => total + item.quizzesAttempted,
      0,
    );

    const totalStudyMinutes = progress.reduce(
      (total, item) => total + item.studyTimeMinutes,
      0,
    );

    const averageScore =
      totalTopics > 0
        ? Number(
            (
              progress.reduce((total, item) => total + item.averageScore, 0) /
              totalTopics
            ).toFixed(2),
          )
        : 0;

    res.status(200).json({
      success: true,
      summary: {
        totalTopics,
        masteredTopics,
        weakTopics,
        totalQuestions,
        totalQuizAttempts,
        totalStudyMinutes,
        averageScore,
      },
    });
  } catch (error) {
    console.error('Progress summary error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to get progress summary',
    });
  }
});

export default router;
