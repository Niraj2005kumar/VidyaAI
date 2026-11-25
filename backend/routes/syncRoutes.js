import express from 'express';
import authMiddleware from '../middleware/authMiddleware.js';
import User from '../models/User.js';
import ChatMessage from '../models/ChatMessage.js';
import ScanRecord from '../models/ScanRecord.js';
import StudySession from '../models/StudySession.js';
import Progress from '../models/Progress.js';
import QuizAttempt from '../models/QuizAttempt.js';
import Quiz from '../models/Quiz.js';
import Opportunity from '../models/Opportunity.js';

const router = express.Router();

router.use(authMiddleware);

// ===============================
// POST /api/sync/push
// ===============================
router.post('/push', async (req, res) => {
  try {
    const userId = req.user.id;
    const {
      settings,
      profile,
      chatMessages = [],
      studySessions = [],
      topicProgress = [],
      quizAttempts = [],
      scans = [],
    } = req.body;

    const accepted = {
      chatMessages: [],
      studySessions: [],
      topicProgress: [],
      quizAttempts: [],
      scans: [],
      profile: false,
      settings: false,
    };

    const rejected = [];

    // 1. Process Profile & Settings
    if (profile || settings) {
      try {
        const user = await User.findById(userId);
        if (user) {
          if (profile && profile.name) user.name = profile.name.trim();
          if (profile && profile.classLevel) user.classLevel = Number(profile.classLevel);
          if (profile && profile.preferredLanguage) user.preferredLanguage = profile.preferredLanguage;
          await user.save();
          accepted.profile = true;
          accepted.settings = true;
        }
      } catch (err) {
        rejected.push({ type: 'profile', message: err.message });
      }
    }

    // 2. Process Chat Messages (bulkWrite upsert)
    if (Array.isArray(chatMessages) && chatMessages.length > 0) {
      const chatOps = [];
      for (const msg of chatMessages) {
        if (!msg.clientId || !msg.text) {
          rejected.push({ type: 'chatMessage', clientId: msg.clientId, message: 'Missing clientId or text' });
          continue;
        }
        chatOps.push({
          updateOne: {
            filter: { user: userId, clientId: msg.clientId },
            update: {
              $set: {
                user: userId,
                clientId: msg.clientId,
                sessionId: msg.sessionId || '',
                role: msg.role || 'user',
                text: msg.text,
                mode: msg.mode || 'STEP_BY_STEP',
                language: msg.language || 'hinglish',
                timestamp: msg.timestamp ? new Date(msg.timestamp) : new Date(),
              },
            },
            upsert: true,
          },
        });
        accepted.chatMessages.push(msg.clientId);
      }
      if (chatOps.length > 0) {
        await ChatMessage.bulkWrite(chatOps, { ordered: false });
      }
    }

    // 3. Process Scan Records (bulkWrite upsert)
    if (Array.isArray(scans) && scans.length > 0) {
      const scanOps = [];
      for (const scan of scans) {
        if (!scan.clientId || !scan.text) {
          rejected.push({ type: 'scan', clientId: scan.clientId, message: 'Missing clientId or text' });
          continue;
        }
        scanOps.push({
          updateOne: {
            filter: { user: userId, clientId: scan.clientId },
            update: {
              $set: {
                user: userId,
                clientId: scan.clientId,
                text: scan.text,
                timestamp: scan.timestamp ? new Date(scan.timestamp) : new Date(),
              },
            },
            upsert: true,
          },
        });
        accepted.scans.push(scan.clientId);
      }
      if (scanOps.length > 0) {
        await ScanRecord.bulkWrite(scanOps, { ordered: false });
      }
    }

    // 4. Process Study Sessions (bulkWrite upsert)
    if (Array.isArray(studySessions) && studySessions.length > 0) {
      const studyOps = [];
      for (const session of studySessions) {
        const cId = session.clientId || `session_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
        if (!session.subject) {
          rejected.push({ type: 'studySession', clientId: cId, message: 'Missing subject' });
          continue;
        }
        studyOps.push({
          updateOne: {
            filter: { user: userId, clientId: cId },
            update: {
              $set: {
                user: userId,
                clientId: cId,
                subject: session.subject,
                topic: session.topic || '',
                startTime: session.startTime ? new Date(session.startTime) : new Date(),
                endTime: session.endTime ? new Date(session.endTime) : undefined,
                durationMinutes: session.durationMinutes || 0,
              },
            },
            upsert: true,
          },
        });
        accepted.studySessions.push(cId);
      }
      if (studyOps.length > 0) {
        await StudySession.bulkWrite(studyOps, { ordered: false });
      }
    }

    // 5. Process Topic Progress (bulkWrite upsert)
    if (Array.isArray(topicProgress) && topicProgress.length > 0) {
      const progressOps = [];
      for (const prog of topicProgress) {
        const cId = prog.clientId || `prog_${prog.subject}_${prog.topic}`;
        if (!prog.subject || !prog.topic) {
          rejected.push({ type: 'topicProgress', clientId: cId, message: 'Missing subject or topic' });
          continue;
        }
        progressOps.push({
          updateOne: {
            filter: { user: userId, clientId: cId },
            update: {
              $set: {
                user: userId,
                clientId: cId,
                classLevel: prog.classLevel || 10,
                subject: prog.subject,
                chapter: prog.chapter || 'General',
                topic: prog.topic,
                questionsAsked: prog.questionsAsked || 0,
                quizzesAttempted: prog.quizzesAttempted || 0,
                averageScore: prog.averageScore || 0,
                studyTimeMinutes: prog.studyTimeMinutes || 0,
                masteryLevel: prog.masteryLevel || 'not_started',
                isWeakArea: Boolean(prog.isWeakArea),
                lastStudiedAt: prog.lastStudiedAt ? new Date(prog.lastStudiedAt) : new Date(),
              },
            },
            upsert: true,
          },
        });
        accepted.topicProgress.push(cId);
      }
      if (progressOps.length > 0) {
        await Progress.bulkWrite(progressOps, { ordered: false });
      }
    }

    // 6. Process Quiz Attempts (bulkWrite upsert)
    if (Array.isArray(quizAttempts) && quizAttempts.length > 0) {
      const attemptOps = [];
      for (const attempt of quizAttempts) {
        const cId = attempt.clientId || `attempt_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
        attemptOps.push({
          updateOne: {
            filter: { user: userId, clientId: cId },
            update: {
              $set: {
                user: userId,
                clientId: cId,
                quizTitle: attempt.quizTitle || attempt.subject || 'Quiz Attempt',
                score: attempt.score || 0,
                totalQuestions: attempt.totalQuestions || 0,
                correctAnswers: attempt.correctAnswers || 0,
                percentage: attempt.percentage || 0,
                answers: attempt.answers || [],
                completedAt: attempt.completedAt ? new Date(attempt.completedAt) : new Date(),
              },
            },
            upsert: true,
          },
        });
        accepted.quizAttempts.push(cId);
      }
      if (attemptOps.length > 0) {
        await QuizAttempt.bulkWrite(attemptOps, { ordered: false });
      }
    }

    res.status(200).json({
      success: true,
      accepted,
      rejected,
      serverTime: new Date().toISOString(),
    });
  } catch (error) {
    console.error('Push sync error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to push sync data',
    });
  }
});

// ===============================
// GET /api/sync/pull?since=<ISO>
// ===============================
router.get('/pull', async (req, res) => {
  try {
    const userId = req.user.id;
    const sinceDate = req.query.since ? new Date(req.query.since) : new Date(0);

    const user = await User.findById(userId).select('-password');

    const quizzes = await Quiz.find({
      updatedAt: { $gt: sinceDate },
    });

    const opportunities = await Opportunity.find({
      updatedAt: { $gt: sinceDate },
    });

    res.status(200).json({
      success: true,
      quizzes,
      opportunities,
      profile: user,
      serverTime: new Date().toISOString(),
    });
  } catch (error) {
    console.error('Pull sync error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to pull sync data',
    });
  }
});

// ===============================
// GET /api/sync/status
// ===============================
router.get('/status', async (req, res) => {
  try {
    const userId = req.user.id;

    const chatCount = await ChatMessage.countDocuments({ user: userId });
    const scanCount = await ScanRecord.countDocuments({ user: userId });
    const sessionCount = await StudySession.countDocuments({ user: userId });
    const progressCount = await Progress.countDocuments({ user: userId });
    const quizCount = await QuizAttempt.countDocuments({ user: userId });

    res.status(200).json({
      success: true,
      counts: {
        chatMessages: chatCount,
        scans: scanCount,
        studySessions: sessionCount,
        topicProgress: progressCount,
        quizAttempts: quizCount,
      },
      serverTime: new Date().toISOString(),
    });
  } catch (error) {
    console.error('Sync status error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch sync status',
    });
  }
});

export default router;
