import express from 'express';
import User from '../models/User.js';
import authMiddleware from '../middleware/authMiddleware.js';

const router = express.Router();

/*
  GET /api/users/profile
  Get logged-in user's profile
*/
router.get('/profile', authMiddleware, async (req, res) => {
  try {
    const user = await User.findById(req.user.id).select('-password');

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found',
      });
    }

    res.status(200).json({
      success: true,
      user,
    });
  } catch (error) {
    console.error('Get profile error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to get user profile',
    });
  }
});

/*
  PUT /api/users/profile
  Update logged-in user's profile
*/
router.put('/profile', authMiddleware, async (req, res) => {
  try {
    const { name, classLevel, preferredLanguage } = req.body;

    const user = await User.findById(req.user.id);

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found',
      });
    }

    if (name !== undefined) {
      user.name = name.trim();
    }

    if (classLevel !== undefined) {
      const parsedClass = Number(classLevel);

      if (
        !Number.isInteger(parsedClass) ||
        parsedClass < 1 ||
        parsedClass > 10
      ) {
        return res.status(400).json({
          success: false,
          message: 'Class level must be between 1 and 10',
        });
      }

      user.classLevel = parsedClass;
    }

    if (preferredLanguage !== undefined) {
      const allowedLanguages = ['english', 'hindi', 'hinglish'];

      if (!allowedLanguages.includes(preferredLanguage)) {
        return res.status(400).json({
          success: false,
          message: 'Invalid preferred language',
        });
      }

      user.preferredLanguage = preferredLanguage;
    }

    const updatedUser = await user.save();

    res.status(200).json({
      success: true,
      message: 'Profile updated successfully',
      user: {
        id: updatedUser._id,
        name: updatedUser.name,
        email: updatedUser.email,
        role: updatedUser.role,
        classLevel: updatedUser.classLevel,
        preferredLanguage: updatedUser.preferredLanguage,
      },
    });
  } catch (error) {
    console.error('Update profile error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to update user profile',
    });
  }
});

export default router;
