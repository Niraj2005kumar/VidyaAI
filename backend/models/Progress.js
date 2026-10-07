import mongoose from 'mongoose';

const progressSchema = new mongoose.Schema(
  {
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true,
    },

    classLevel: {
      type: Number,
      required: true,
      min: 1,
      max: 10,
    },

    subject: {
      type: String,
      required: true,
      trim: true,
    },

    chapter: {
      type: String,
      trim: true,
    },

    topic: {
      type: String,
      required: true,
      trim: true,
    },

    questionsAsked: {
      type: Number,
      default: 0,
      min: 0,
    },

    quizzesAttempted: {
      type: Number,
      default: 0,
      min: 0,
    },

    averageScore: {
      type: Number,
      default: 0,
      min: 0,
      max: 100,
    },

    studyTimeMinutes: {
      type: Number,
      default: 0,
      min: 0,
    },

    masteryLevel: {
      type: String,
      enum: ['not_started', 'beginner', 'learning', 'good', 'mastered'],
      default: 'not_started',
    },

    isWeakArea: {
      type: Boolean,
      default: false,
    },

    lastStudiedAt: {
      type: Date,
    },
  },
  {
    timestamps: true,
  },
);

const Progress = mongoose.model('Progress', progressSchema);

export default Progress;
