# Database Connection Testing Instructions

This document provides step-by-step instructions on how to test your MongoDB database connection.

## Prerequisites

Make sure you have the following installed:
- Node.js (v14 or higher)
- npm (comes with Node.js)

## Steps to Test Database Connection

### 1. Install Dependencies

First, make sure all dependencies are installed:

```bash
npm install
```

This will install all required packages, including the newly added `dotenv` and `ts-node` packages.

### 2. Verify Environment Variables

Ensure your `.env` file contains the correct MongoDB URI:

```
MONGO_URI=mongodb+srv://your_username:your_password@your_cluster_url
```

The current configuration is using:
```
MONGO_URI=mongodb+srv://elite:HCKKNk0iNxxqt027@cluster0.wabnsto.mongodb.net/?appName=Cluster0
```

### 3. Run the Test Script

Execute the database connection test script using one of the following commands:

```bash
npm run test-db
```

If you encounter an error about "Unknown file extension '.ts'", try one of these alternative commands:

```bash
# Alternative 1: Using CommonJS configuration
npm run test-db-cjs

# Alternative 2: Using the JavaScript version
npm run test-db-js
```

### 4. Interpret Results

- **Success**: If the connection is successful, you'll see:
  ```
  Testing database connection...
  Connected to database development - mongodb+srv://...
  Database connection successful!
  ```

- **Failure**: If the connection fails, you'll see an error message explaining the issue, such as:
  ```
  Testing database connection...
  Database connection failed: [error message]
  ```

## Troubleshooting

### Common Connection Issues

If the connection fails, check the following:

1. **Internet Connection**: Ensure you have an active internet connection.

2. **MongoDB URI**: Verify that the MongoDB URI in your `.env` file is correct:
   - Username and password are correct
   - Cluster name is correct
   - No typos in the URI

3. **MongoDB Atlas**: If using MongoDB Atlas:
   - Ensure your IP address is whitelisted in the Atlas dashboard
   - Verify that your MongoDB user has the correct permissions

4. **Firewall Issues**: Check if your firewall is blocking the connection.

### TypeScript Module Resolution Issues

If you encounter the error `TypeError: Unknown file extension ".ts"`, this is related to how Node.js handles TypeScript files in ESM (ECMAScript Modules) mode. We've provided three different ways to run the test script to address this issue:

1. **ESM Mode** (`npm run test-db`): Uses the `--esm` flag with ts-node to explicitly enable ESM module resolution for TypeScript files.

2. **CommonJS Mode** (`npm run test-db-cjs`): Uses a separate TypeScript configuration (tsconfig.node.json) that's specifically set up for CommonJS modules, which is more compatible with ts-node's default behavior.

3. **JavaScript Version** (`npm run test-db-js`): Uses the pre-compiled JavaScript version of the test script, bypassing TypeScript compilation entirely.

## Manual Testing Alternative

If you prefer to test the connection manually in code, you can create a new file with the following content:

```typescript
import 'dotenv/config';
import { connectToDatabase } from './database/mongoose';

async function testConnection() {
  try {
    await connectToDatabase();
    console.log('Connection successful!');
  } catch (error) {
    console.error('Connection failed:', error);
  }
}

testConnection();
```

Then run it with one of these commands:
```bash
# ESM mode
npx ts-node --esm your-test-file.ts

# CommonJS mode
npx ts-node --project tsconfig.node.json your-test-file.ts

# Or compile to JavaScript first and run with Node.js
npx tsc your-test-file.ts && node your-test-file.js
```
