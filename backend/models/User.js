import mongoose from 'mongoose';

const userSchema = new mongoose.Schema(
  {
    deviceId: {
      type: String,
      unique: true,
      sparse: true,
      trim: true,
    },

    name: {
      type: String,
      required: false,
      trim: true,
      default: 'Guest Student',
    },

    email: {
      type: String,
      required: false,
      unique: true,
      sparse: true,
      lowercase: true,
      trim: true,
    },

    password: {
      type: String,
      required: false,
      minlength: 6,
    },

    role: {
      type: String,
      enum: ['student', 'parent', 'teacher', 'admin'],
      default: 'student',
    },

    classLevel: {
      type: Number,
      min: 1,
      max: 10,
      default: 10,
    },

    preferredLanguage: {
      type: String,
      enum: ['english', 'hindi', 'hinglish'],
      default: 'english',
    },
  },
  {
    timestamps: true,
  },
);

const User = mongoose.model('User', userSchema);

export default User;
