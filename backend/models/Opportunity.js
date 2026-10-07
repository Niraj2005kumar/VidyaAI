import mongoose from 'mongoose';

const opportunitySchema = new mongoose.Schema(
  {
    title: {
      type: String,
      required: true,
      trim: true,
    },

    description: {
      type: String,
      required: true,
    },

    type: {
      type: String,
      enum: [
        'scholarship',
        'exam',
        'hackathon',
        'competition',
        'admission',
        'other',
      ],
      required: true,
    },

    classLevels: [
      {
        type: Number,
        min: 1,
        max: 10,
      },
    ],

    organization: {
      type: String,
      trim: true,
    },

    eligibility: {
      type: String,
    },

    applicationStartDate: {
      type: Date,
    },

    applicationDeadline: {
      type: Date,
    },

    officialLink: {
      type: String,
      trim: true,
    },

    isActive: {
      type: Boolean,
      default: true,
    },

    isFeatured: {
      type: Boolean,
      default: false,
    },
  },
  {
    timestamps: true,
  },
);

const Opportunity = mongoose.model('Opportunity', opportunitySchema);

export default Opportunity;
