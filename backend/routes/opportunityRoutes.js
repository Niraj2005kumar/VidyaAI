import express from 'express';
import Opportunity from '../models/Opportunity.js';
import authMiddleware from '../middleware/authMiddleware.js';

const router = express.Router();

// ======================================
// CREATE OPPORTUNITY
// ======================================
router.post('/', authMiddleware, async (req, res) => {
  try {
    const {
      title,
      description,
      type,
      classLevels,
      organization,
      eligibility,
      applicationStartDate,
      applicationDeadline,
      officialLink,
      isFeatured,
    } = req.body;

    if (!title || !description || !type) {
      return res.status(400).json({
        success: false,
        message: 'Title, description and type are required',
      });
    }

    const opportunity = await Opportunity.create({
      title,
      description,
      type,
      classLevels,
      organization,
      eligibility,
      applicationStartDate,
      applicationDeadline,
      officialLink,
      isFeatured: isFeatured || false,
    });

    res.status(201).json({
      success: true,
      message: 'Opportunity created successfully',
      opportunity,
    });
  } catch (error) {
    console.error('Create opportunity error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to create opportunity',
    });
  }
});

// ======================================
// GET ALL ACTIVE OPPORTUNITIES
// ======================================
router.get('/', async (req, res) => {
  try {
    const { type, classLevel, featured } = req.query;

    const filter = {
      isActive: true,
    };

    if (type) {
      filter.type = type;
    }

    if (classLevel) {
      filter.classLevels = Number(classLevel);
    }

    if (featured === 'true') {
      filter.isFeatured = true;
    }

    const opportunities = await Opportunity.find(filter).sort({
      applicationDeadline: 1,
    });

    res.status(200).json({
      success: true,
      count: opportunities.length,
      opportunities,
    });
  } catch (error) {
    console.error('Get opportunities error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to get opportunities',
    });
  }
});

// ======================================
// GET SINGLE OPPORTUNITY
// ======================================
router.get('/:opportunityId', async (req, res) => {
  try {
    const opportunity = await Opportunity.findOne({
      _id: req.params.opportunityId,
      isActive: true,
    });

    if (!opportunity) {
      return res.status(404).json({
        success: false,
        message: 'Opportunity not found',
      });
    }

    res.status(200).json({
      success: true,
      opportunity,
    });
  } catch (error) {
    console.error('Get opportunity error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to get opportunity',
    });
  }
});

// ======================================
// GET UPCOMING DEADLINES
// ======================================
router.get('/upcoming/deadlines', async (req, res) => {
  try {
    const today = new Date();

    const opportunities = await Opportunity.find({
      isActive: true,
      applicationDeadline: {
        $gte: today,
      },
    })
      .sort({
        applicationDeadline: 1,
      })
      .limit(20);

    res.status(200).json({
      success: true,
      count: opportunities.length,
      opportunities,
    });
  } catch (error) {
    console.error('Get upcoming deadlines error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to get upcoming deadlines',
    });
  }
});

export default router;
