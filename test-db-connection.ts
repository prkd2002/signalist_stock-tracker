// Test script for MongoDB connection
import 'dotenv/config';
import { connectToDatabase } from './database/mongoose';

async function testConnection() {
  console.log('Testing database connection...');
  try {
    await connectToDatabase();
    console.log('Database connection successful!');
    process.exit(0);
  } catch (error) {
    console.error('Database connection failed:', error instanceof Error ? error.message : String(error));
    process.exit(1);
  }
}

testConnection();