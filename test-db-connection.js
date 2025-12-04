// Test script for MongoDB connection
require('dotenv').config();
const { connectToDatabase } = require('./database/mongoose');

async function testConnection() {
  console.log('Testing database connection...');
  try {
    await connectToDatabase();
    console.log('Database connection successful!');
    process.exit(0);
  } catch (error) {
    console.error('Database connection failed:', error.message);
    process.exit(1);
  }
}

testConnection();