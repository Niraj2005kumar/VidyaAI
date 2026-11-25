import mongoose from 'mongoose';

const chatMessageSchema = new mongoose.Schema(
  {
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true,
    },
    clientId: {
      type: String,
      required: true,
      trim: true,
    },
    sessionId: {
      type: String,
      required: false,
    },
    role: {
      type: String,
      enum: ['user', 'assistant', 'system'],
      default: 'user',
    },
    text: {
      type: String,
      required: true,
    },
    mode: {
      type: String,
      default: 'STEP_BY_STEP',
    },
    language: {
      type: String,
      default: 'hinglish',
    },
    timestamp: {
      type: Date,
      default: Date.now,
    },
  },
  {
    timestamps: true,
  },
);

chatMessageSchema.index({ user: 1, clientId: 1 }, { unique: true });

const ChatMessage = mongoose.model('ChatMessage', chatMessageSchema);

export default ChatMessage;
