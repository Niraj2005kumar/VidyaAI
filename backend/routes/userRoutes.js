import express from 'express';
import User from '../models/User.js';
import authMiddleware from '../middleware/authMiddleware.js';

const router = express.Router();

// ======================================
// GET CURRENT USER PROFILE
// ======================================
router.get('/profile', authMiddleware, async (req, res) => {
  try {
    const user = await User.findById(req.user.userId).select('-password');

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
      message: 'Failed to get profile',
    });
  }
});

// ======================================
// UPDATE CURRENT USER PROFILE
// ======================================
router.put('/profile', authMiddleware, async (req, res) => {
  try {
    const { name, classLevel, preferredLanguage } = req.body;

    const user = await User.findById(req.user.userId);

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found',
      });
    }

    if (name !== undefined) {
      user.name = name;
    }

    if (classLevel !== undefined) {
      user.classLevel = classLevel;
    }

    if (preferredLanguage !== undefined) {
      user.preferredLanguage = preferredLanguage;
    }

    await user.save();

    res.status(200).json({
      success: true,
      message: 'Profile updated successfully',
      user: {
        id: user._id,
        name: user.name,
        email: user.email,
        role: user.role,
        classLevel: user.classLevel,
        preferredLanguage: user.preferredLanguage,
      },
    });
  } catch (error) {
    console.error('Update profile error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to update profile',
    });
  }
});

export default router;
