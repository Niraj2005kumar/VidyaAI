import express from 'express';
import Opportunity from '../models/Opportunity.js';
import authMiddleware from '../middleware/authMiddleware.js';

const router = express.Router();

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
      isActive,
      isFeatured,
    } = req.body;

    if (!title || !description || !type) {
      return res.status(400).json({
        success: false,
        message: 'Title, description and type are required',
      });
    }

    const allowedTypes = [
      'scholarship',
      'exam',
      'hackathon',
      'competition',
      'admission',
      'other',
    ];

    if (!allowedTypes.includes(type)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid opportunity type',
      });
    }

    const opportunity = await Opportunity.create({
      title: title.trim(),
      description: description.trim(),
      type,
      classLevels: Array.isArray(classLevels) ? classLevels.map(Number) : [],
      organization: organization?.trim() || '',
      eligibility: eligibility?.trim() || '',
      applicationStartDate: applicationStartDate || null,
      applicationDeadline: applicationDeadline || null,
      officialLink: officialLink?.trim() || '',
      isActive: isActive !== undefined ? Boolean(isActive) : true,
      isFeatured: isFeatured !== undefined ? Boolean(isFeatured) : false,
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

router.get('/', authMiddleware, async (req, res) => {
  try {
    const filter = {
      isActive: true,
    };

    if (req.query.type) {
      filter.type = req.query.type;
    }

    if (req.query.classLevel) {
      filter.classLevels = Number(req.query.classLevel);
    }

    const opportunities = await Opportunity.find(filter).sort({
      isFeatured: -1,
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
      message: 'Failed to fetch opportunities',
    });
  }
});

router.get('/:id', authMiddleware, async (req, res) => {
  try {
    const opportunity = await Opportunity.findById(req.params.id);

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
      message: 'Failed to fetch opportunity',
    });
  }
});

router.get('/upcoming/deadlines', authMiddleware, async (req, res) => {
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
    console.error('Upcoming deadlines error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to fetch upcoming deadlines',
    });
  }
});

router.put('/:id', authMiddleware, async (req, res) => {
  try {
    const allowedFields = [
      'title',
      'description',
      'type',
      'classLevels',
      'organization',
      'eligibility',
      'applicationStartDate',
      'applicationDeadline',
      'officialLink',
      'isActive',
      'isFeatured',
    ];

    const updates = {};

    for (const field of allowedFields) {
      if (req.body[field] !== undefined) {
        updates[field] = req.body[field];
      }
    }

    if (updates.classLevels) {
      updates.classLevels = updates.classLevels.map(Number);
    }

    const opportunity = await Opportunity.findByIdAndUpdate(
      req.params.id,
      updates,
      {
        new: true,
        runValidators: true,
      },
    );

    if (!opportunity) {
      return res.status(404).json({
        success: false,
        message: 'Opportunity not found',
      });
    }

    res.status(200).json({
      success: true,
      message: 'Opportunity updated successfully',
      opportunity,
    });
  } catch (error) {
    console.error('Update opportunity error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to update opportunity',
    });
  }
});

router.delete('/:id', authMiddleware, async (req, res) => {
  try {
    const opportunity = await Opportunity.findByIdAndDelete(req.params.id);

    if (!opportunity) {
      return res.status(404).json({
        success: false,
        message: 'Opportunity not found',
      });
    }

    res.status(200).json({
      success: true,
      message: 'Opportunity deleted successfully',
    });
  } catch (error) {
    console.error('Delete opportunity error:', error);

    res.status(500).json({
      success: false,
      message: 'Failed to delete opportunity',
    });
  }
});

export default router;
