import mongoose from 'mongoose';

const quizSchema = new mongoose.Schema(
  {
    title: {
      type: String,
      required: true,
      trim: true,
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

    questions: [
      {
        question: {
          type: String,
          required: true,
        },

        options: [
          {
            type: String,
            required: true,
          },
        ],

        correctAnswer: {
          type: String,
          required: true,
        },

        explanation: {
          type: String,
        },

        difficulty: {
          type: String,
          enum: ['easy', 'medium', 'hard'],
          default: 'easy',
        },
      },
    ],

    createdBy: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
    },
  },
  {
    timestamps: true,
  },
);

const Quiz = mongoose.model('Quiz', quizSchema);

export default Quiz;
