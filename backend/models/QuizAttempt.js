import mongoose from 'mongoose';

const quizAttemptSchema = new mongoose.Schema(
  {
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true,
    },

    clientId: {
      type: String,
      required: false,
      trim: true,
    },

    quiz: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Quiz',
      required: false,
    },

    quizTitle: {
      type: String,
      default: 'Quiz Attempt',
    },

    answers: [
      {
        questionId: {
          type: String,
        },

        selectedAnswer: {
          type: String,
        },

        correct: {
          type: Boolean,
          default: false,
        },
      },
    ],

    score: {
      type: Number,
      default: 0,
      min: 0,
    },

    totalQuestions: {
      type: Number,
      default: 0,
      min: 0,
    },

    correctAnswers: {
      type: Number,
      default: 0,
      min: 0,
    },

    percentage: {
      type: Number,
      default: 0,
      min: 0,
      max: 100,
    },

    completedAt: {
      type: Date,
      default: Date.now,
    },
  },
  {
    timestamps: true,
  },
);

quizAttemptSchema.index({ user: 1, clientId: 1 }, { unique: true, sparse: true });

const QuizAttempt = mongoose.model('QuizAttempt', quizAttemptSchema);

export default QuizAttempt;
