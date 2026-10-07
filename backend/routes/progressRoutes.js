import express from 'express';
import Progress from '../models/Progress.js';
import authMiddleware from '../middleware/authMiddleware.js';

const router = express.Router();

/*
  POST /api/progress/update
  Create or update topic progress
*/
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

    if (!classLevel || !subject || !chapter || !topic) {
      return res.status(400).json({
        success: false,
        message: 'Class, subject, chapter and topic are required',
      });
    }

    let progress = await Progress.findOne({
      user: req.user.id,
      classLevel: Number(classLevel),
      subject: subject.trim(),
      chapter: chapter.trim(),
      topic: topic.trim(),
    });

    if (!progress) {
      progress = new Progress({
        user: req.user.id,
        classLevel: Number(classLevel),
        subject: subject.trim(),
        chapter: chapter.trim(),
        topic: topic.trim(),
      });
    }

    if (questionsAsked !== undefined) {
      progress.questionsAsked = Number(questionsAsked);
    }

    if (quizzesAttempted !== undefined) {
      progress.quizzesAttempted = Number(quizzesAttempted);
    }

    if (averageScore !== undefined) {
      progress.averageScore = Number(averageScore);
    }

    if (studyTimeMinutes !== undefined) {
      progress.studyTimeMinutes = Number(studyTimeMinutes);
    }

    if (masteryLevel !== undefined) {
      const allowedLevels = [
        'not_started',
        'beginner',
        'learning',
        'good',
        'mastered',
      ];

      if (!allowedLevels.includes(masteryLevel)) {
        return res.status(400).json({
          success: false,
          message: 'Invalid mastery level',
        });
      }

      progress.masteryLevel = masteryLevel;
    }

    if (isWeakArea !== undefined) {
      progress.isWeakArea = Boolean(isWeakArea);
    }

    progress.lastStudiedAt = new Date();

    await progress.save();

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


router.get('/', authMiddleware, async (req, res) => {
  try {
    const filter = {
      user: req.user.id,
    };

    if (req.query.classLevel) {
      filter.classLevel = Number(req.query.classLevel);
    }

    if (req.query.subject) {
      filter.subject = req.query.subject;
    }

    const progress = await Progress.find(filter).sort({ lastStudiedAt: -1 });

    res.status(200).json({
      success: true,
      count: progress.length,
      progress,
    });
  } catch (error) {
    console.error('Get progress error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to fetch progress',
    });
  }
});

/*
  GET /api/progress/weak-areas
  Get weak topics
*/
router.get('/weak-areas', authMiddleware, async (req, res) => {
  try {
    const weakAreas = await Progress.find({
      user: req.user.id,
      isWeakArea: true,
    }).sort({
      lastStudiedAt: -1,
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
      message: 'Failed to fetch weak areas',
    });
  }
});

/*
  GET /api/progress/subject/:subject
  Get progress for a particular subject
*/
router.get('/subject/:subject', authMiddleware, async (req, res) => {
  try {
    const progress = await Progress.find({
      user: req.user.id,
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
      message: 'Failed to fetch subject progress',
    });
  }
});

/*
  GET /api/progress/summary
  Get overall progress summary
*/
router.get('/summary', authMiddleware, async (req, res) => {
  try {
    const progress = await Progress.find({
      user: req.user.id,
    });

    const totalTopics = progress.length;

    const masteredTopics = progress.filter(
      (item) => item.masteryLevel === 'mastered',
    ).length;

    const learningTopics = progress.filter(
      (item) =>
        item.masteryLevel === 'learning' || item.masteryLevel === 'beginner',
    ).length;

    const weakAreas = progress.filter((item) => item.isWeakArea).length;

    const totalQuestionsAsked = progress.reduce(
      (sum, item) => sum + (item.questionsAsked || 0),
      0,
    );

    const totalQuizzesAttempted = progress.reduce(
      (sum, item) => sum + (item.quizzesAttempted || 0),
      0,
    );

    const totalStudyTimeMinutes = progress.reduce(
      (sum, item) => sum + (item.studyTimeMinutes || 0),
      0,
    );

    const averageScore =
      progress.length > 0
        ? Number(
            (
              progress.reduce(
                (sum, item) => sum + (item.averageScore || 0),
                0,
              ) / progress.length
            ).toFixed(2),
          )
        : 0;

    res.status(200).json({
      success: true,
      summary: {
        totalTopics,
        masteredTopics,
        learningTopics,
        weakAreas,
        totalQuestionsAsked,
        totalQuizzesAttempted,
        totalStudyTimeMinutes,
        averageScore,
      },
    });
  } catch (error) {
    console.error('Progress summary error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to generate progress summary',
    });
  }
});

export default router;
