import express from 'express';
import StudySession from '../models/StudySession.js';
import authMiddleware from '../middleware/authMiddleware.js';

const router = express.Router();

// ======================================
// START STUDY SESSION
// ======================================
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
      user: req.user.userId,
      subject,
      topic,
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

// ======================================
// END STUDY SESSION
// ======================================
router.put('/:sessionId/end', authMiddleware, async (req, res) => {
  try {
    const session = await StudySession.findOne({
      _id: req.params.sessionId,
      user: req.user.userId,
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
        message: 'Study session has already ended',
      });
    }

    const endTime = new Date();

    const durationMilliseconds =
      endTime.getTime() - session.startTime.getTime();

    const durationMinutes = Math.max(
      0,
      Math.round(durationMilliseconds / (1000 * 60)),
    );

    session.endTime = endTime;
    session.durationMinutes = durationMinutes;

    await session.save();

    res.status(200).json({
      success: true,
      message: 'Study session ended',
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

// ======================================
// GET USER STUDY HISTORY
// ======================================
router.get('/history', authMiddleware, async (req, res) => {
  try {
    const sessions = await StudySession.find({
      user: req.user.userId,
    }).sort({
      startTime: -1,
    });

    res.status(200).json({
      success: true,
      count: sessions.length,
      sessions,
    });
  } catch (error) {
    console.error('Study history error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to get study history',
    });
  }
});

// ======================================
// GET TOTAL STUDY TIME
// ======================================
router.get('/total-time', authMiddleware, async (req, res) => {
  try {
    const result = await StudySession.aggregate([
      {
        $match: {
          user: req.user.userId,
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
