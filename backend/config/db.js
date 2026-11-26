import dns from 'node:dns';
import mongoose from 'mongoose';

const connectDB = async () => {
  const mongoUri = process.env.MONGO_URI;

  if (!mongoUri) {
    throw new Error('MONGO_URI is not set. Add it to backend/.env.');
  }

  const dnsServers = process.env.MONGO_DNS_SERVERS
    ?.split(',')
    .map((server) => server.trim())
    .filter(Boolean);

  if (dnsServers?.length) {
    dns.setServers(dnsServers);
  }

  const connection = await mongoose.connect(mongoUri);

  console.log(`✅ MongoDB connected: ${connection.connection.host}`);
};

export default connectDB;
