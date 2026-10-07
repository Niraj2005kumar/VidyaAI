import express from 'express';
import authMiddleware from '../middleware/authMiddleware.js';
import StudySession from '../models/StudySession.js';

const router = express.Router();

// Start a study session
router.post('/start', authMiddleware, async (req, res) => {
  try {
    const { subject, topic } = req.body;

    if (!subject) {
      return res.status(400).json({
        success: false,
        message: 'Subject is required',
      });
    }

    const session = await StudySession.create({
      user: req.user.id,
      subject,
      topic: topic || null,
      startTime: new Date(),
      durationMinutes: 0,
    });

    res.status(201).json({
      success: true,
      message: 'Study session started',
      data: session,
    });
  } catch (error) {
    console.error('Start study session error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to start study session',
    });
  }
});

// End a study session
router.put('/end/:id', authMiddleware, async (req, res) => {
  try {
    const session = await StudySession.findOne({
      _id: req.params.id,
      user: req.user.id,
    });

    if (!session) {
      return res.status(404).json({
        success: false,
        message: 'Study session not found',
      });
    }

    if (session.endTime) {
      return res.status(400).json({
        success: false,
        message: 'Study session already ended',
      });
    }

    const endTime = new Date();

    const durationMinutes = Math.max(
      0,
      Math.round((endTime.getTime() - session.startTime.getTime()) / 60000),
    );

    session.endTime = endTime;
    session.durationMinutes = durationMinutes;

    await session.save();

    res.json({
      success: true,
      message: 'Study session ended',
      data: session,
    });
  } catch (error) {
    console.error('End study session error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to end study session',
    });
  }
});

// Get user's study history
router.get('/history', authMiddleware, async (req, res) => {
  try {
    const sessions = await StudySession.find({
      user: req.user.id,
    }).sort({ startTime: -1 });

    res.json({
      success: true,
      count: sessions.length,
      data: sessions,
    });
  } catch (error) {
    console.error('Study history error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to fetch study history',
    });
  }
});

// Get total study time
router.get('/total-time', authMiddleware, async (req, res) => {
  try {
    const sessions = await StudySession.find({
      user: req.user.id,
    });

    const totalMinutes = sessions.reduce(
      (total, session) => total + (session.durationMinutes || 0),
      0,
    );

    res.json({
      success: true,
      data: {
        totalStudyTimeMinutes: totalMinutes,
        totalStudyTimeHours: Number((totalMinutes / 60).toFixed(2)),
      },
    });
  } catch (error) {
    console.error('Total study time error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to calculate study time',
    });
  }
});

export default router;
