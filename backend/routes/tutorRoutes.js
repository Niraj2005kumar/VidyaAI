import express from 'express';
import authMiddleware from '../middleware/authMiddleware.js';

const router = express.Router();

/*
    POST /api/tutor/question

    This endpoint stores/handles tutor request metadata.
    Actual AI inference will remain on-device.
*/

router.post('/question', authMiddleware, async (req, res) => {
  try {
    const {
      question,
      classLevel,
      subject,
      chapter,
      topic,
      language,
      teachingStyle,
    } = req.body;

    if (!question || !question.trim()) {
      return res.status(400).json({
        success: false,
        message: 'Question is required',
      });
    }

    if (!classLevel) {
      return res.status(400).json({
        success: false,
        message: 'Class level is required',
      });
    }

    if (classLevel < 1 || classLevel > 10) {
      return res.status(400).json({
        success: false,
        message: 'Class level must be between 1 and 10',
      });
    }

    res.json({
      success: true,
      message: 'Tutor question received',
      data: {
        question,
        classLevel,
        subject: subject || null,
        chapter: chapter || null,
        topic: topic || null,
        language: language || 'english',
        teachingStyle: teachingStyle || 'step-by-step',
        inferenceMode: 'on-device',
      },
    });
  } catch (error) {
    console.error('Tutor question error:', error.message);

    res.status(500).json({
      success: false,
      message: 'Failed to process tutor request',
    });
  }
});

/*
    GET /api/tutor/status

    Returns information about the AI architecture.
*/

router.get('/status', authMiddleware, async (req, res) => {
  res.json({
    success: true,
    data: {
      tutorName: 'ViyaAI',
      mode: 'offline',
      inference: 'on-device',
      model: 'Qwen2.5-1.5B',
      quantization: 'Q4_K_M',
      format: 'GGUF',
    },
  });
});

export default router;
