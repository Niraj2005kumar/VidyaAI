import express from 'express';
import authMiddleware from '../middleware/authMiddleware.js';

const router = express.Router();

router.post('/question', authMiddleware, async (req, res) => {
  try {
    const { question, classLevel, subject, topic, language, teachingStyle } =
      req.body;

    if (!question || !question.trim()) {
      return res.status(400).json({
        success: false,
        message: 'Question is required',
      });
    }

    const validLanguages = ['english', 'hindi', 'hinglish'];

    const validStyles = [
      'simple',
      'detailed',
      'step-by-step',
      'example-based',
      'exam-ready',
      'basic',
    ];

    const selectedLanguage = validLanguages.includes(language)
      ? language
      : 'english';

    const selectedStyle = validStyles.includes(teachingStyle)
      ? teachingStyle
      : 'simple';

    res.status(200).json({
      success: true,
      message: 'Question received',
      data: {
        question: question.trim(),
        classLevel: classLevel ? Number(classLevel) : null,
        subject: subject?.trim() || null,
        topic: topic?.trim() || null,
        language: selectedLanguage,
        teachingStyle: selectedStyle,
        userId: req.user.id,
        mode: 'offline',
        aiEngine: 'on-device',
      },
    });
  } catch (error) {
    console.error('Tutor question error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to process tutor request',
    });
  }
});

router.get('/status', authMiddleware, async (req, res) => {
  try {
    res.status(200).json({
      success: true,
      status: 'ready',
      mode: 'offline',
      aiEngine: 'on-device',
      internetRequired: false,
    });
  } catch (error) {
    console.error('Tutor status error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to get tutor status',
    });
  }
});

export default router;
