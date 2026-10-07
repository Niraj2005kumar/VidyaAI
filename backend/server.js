import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import dotenv from 'dotenv';

dotenv.config();

const app = express();

const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());
app.use(helmet());
app.use(morgan('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Root Route
app.get('/', (req, res) => {
  res.json({
    success: true,
    message: 'VidyaNova Backend API is running',
    version: '1.0.0',
  });
});

// Health Check
app.get('/api/health', (req, res) => {
  res.json({
    success: true,
    status: 'OK',
    message: 'VidyaNova backend is healthy',
  });
});

// Start Server
app.listen(PORT, () => {
  console.log(`🚀 VidyaNova Backend running on port ${PORT}`);
  console.log(`📡 Local URL: http://localhost:${PORT}`);
});
