import mongoose from 'mongoose';

const scanRecordSchema = new mongoose.Schema(
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
    text: {
      type: String,
      required: true,
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

scanRecordSchema.index({ user: 1, clientId: 1 }, { unique: true });

const ScanRecord = mongoose.model('ScanRecord', scanRecordSchema);

export default ScanRecord;
