import express from 'express';
import StudySession from '../models/StudySession.js';
import authMiddleware from '../middleware/authMiddleware.js';

const router = express.Router();

/*
  POST /api/study/start
  Start a new study session
*/
router.post('/start', authMiddleware, async (req, res) => {
  try {
    const { subject, topic } = req.body;

    if (!subject || !topic) {
      return res.status(400).json({
        success: false,
        message: 'Subject and topic are required',
      });
    }

    const session = await StudySession.create({
      user: req.user.id,
      subject: subject.trim(),
      topic: topic.trim(),
      startTime: new Date(),
    });

    res.status(201).json({
      success: true,
      message: 'Study session started',
      session,
    });
  } catch (error) {
    console.error('Start study session error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to start study session',
    });
  }
});

/*
  PUT /api/study/end/:id
  End a study session
*/
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
        message: 'Study session is already completed',
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

    res.status(200).json({
      success: true,
      message: 'Study session completed',
      session,
    });
  } catch (error) {
    console.error('End study session error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to end study session',
    });
  }
});

/*
  GET /api/study/history
  Get logged-in user's study history
*/
router.get('/history', authMiddleware, async (req, res) => {
  try {
    const sessions = await StudySession.find({
      user: req.user.id,
    })
      .sort({ startTime: -1 })
      .limit(100);

    res.status(200).json({
      success: true,
      count: sessions.length,
      sessions,
    });
  } catch (error) {
    console.error('Study history error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to fetch study history',
    });
  }
});

/*
  GET /api/study/total-time
  Get total study time
*/
router.get('/total-time', authMiddleware, async (req, res) => {
  try {
    const result = await StudySession.aggregate([
      {
        $match: {
          user: req.user.id,
        },
      },
      {
        $group: {
          _id: null,
          totalMinutes: {
            $sum: '$durationMinutes',
          },
        },
      },
    ]);

    const totalMinutes = result.length > 0 ? result[0].totalMinutes : 0;

    res.status(200).json({
      success: true,
      totalMinutes,
      totalHours: Number((totalMinutes / 60).toFixed(2)),
    });
  } catch (error) {
    console.error('Total study time error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to calculate total study time',
    });
  }
});

export default router;
