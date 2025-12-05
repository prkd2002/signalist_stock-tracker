// ORIGINAL CODE WITH EXPLANATIONS

// This line imports necessary components from the mongoose library:
// - model: Function to create a new model
// - Model: TypeScript interface for type checking
// - models: Object containing all registered models
// - Schema: Class to define the structure of documents
import {model, Model, models, Schema} from "mongoose";

// This interface defines the structure of a WatchlistItem document
// It extends the Document interface from mongoose, which provides
// additional properties and methods for database documents
export interface WatchlistItem extends Document {
    userId: string;    // Stores the ID of the user who owns this watchlist item
    symbol: string;    // Stores the stock symbol (e.g., AAPL for Apple)
    company: string;   // Stores the company name
    addedAt: Date;     // Stores when the item was added to the watchlist
}

// This creates a new Schema for the WatchlistItem
// The Schema defines the structure, validations, and indexes for the MongoDB collection
const WatchlistSchema = new Schema<WatchlistItem>(
    {
        // userId field definition:
        // - type: String - defines the data type
        // - required: true - this field must be provided
        // - index: true - creates an index for faster queries by userId
        userId: {type: String, required: true, index: true},
        
        // symbol field definition:
        // - type: String - defines the data type
        // - required: true - this field must be provided
        // - uppercase: true - automatically converts values to uppercase
        // - trim: true - removes whitespace from both ends of the string
        symbol: {type: String, required: true, uppercase: true, trim: true},
        
        // company field definition:
        // - type: String - defines the data type
        // - required: true - this field must be provided
        // - trim: true - removes whitespace from both ends of the string
        company: {type: String, required: true, trim: true},
        
        // addedAt field definition:
        // - type: Date - defines the data type
        // - default: Date.now - automatically sets the current date/time if not provided
        addedAt: {type: Date, default: Date.now}
    },
    {
        // Schema options:
        // - timestamps: false - disables automatic createdAt and updatedAt fields
        timestamps: false
    }
);

// This creates a compound index on userId and symbol fields
// - {userId: 1, symbol: 1} - creates an ascending index on both fields
// - {unique: true} - ensures that the combination of userId and symbol is unique
//   This prevents a user from adding the same stock symbol to their watchlist twice
WatchlistSchema.index({userId: 1, symbol: 1}, {unique: true});

// This exports the Watchlist model
// The expression does the following:
// 1. Check if a Watchlist model already exists in the 'models' object
//    (models?.Watchlist as Model<WatchlistItem>)
// 2. If it exists, use that model to prevent redefining it
// 3. If it doesn't exist, create a new model with:
//    - 'Watchlist' as the model name (and collection name in MongoDB)
//    - WatchlistSchema as the schema definition
// This pattern is commonly used in Next.js to prevent model redefinition during hot reloading
export const Watchlist: Model<WatchlistItem> = (models?.Watchlist as Model<WatchlistItem>) || model<WatchlistItem>('Watchlist', WatchlistSchema);