const express = require('express');
const mongoose = require('mongoose');
const bodyParser = require('body-parser');
const cors = require('cors');

// Create an express app
const app = express();
const port = 3000;

// Middleware
app.use(cors());  // Enable CORS
app.use(bodyParser.json());  // Parse incoming JSON data

// MongoDB connection (Updated to remove deprecated options)
mongoose.connect('mongodb://localhost/crud_db')
  .then(() => {
    console.log('Connected to MongoDB');
  })
  .catch((error) => {
    console.error('Error connecting to MongoDB:', error);
  });

// MongoDB Schema for 'users' collection
const userSchema = new mongoose.Schema({
  name: { type: String, required: true },
  email: { type: String, required: true }
});

// Create a Mongoose model for the users
const User = mongoose.model('User', userSchema);

// API Routes
// Get all users
app.get('/api/users', (req, res) => {
  User.find()
    .then(users => {
      res.json(users);
    })
    .catch(err => {
      res.status(500).json({ success: false, message: err.message });
    });
});

// Get a single user by ID for editing
app.get('/api/users/:id', (req, res) => {
  const userId = req.params.id;
  User.findById(userId)
    .then(user => {
      if (!user) {
        return res.status(404).json({ success: false, message: 'User not found.' });
      }
      res.json(user);
    })
    .catch(err => {
      res.status(500).json({ success: false, message: err.message });
    });
});

// Add a new user
app.post('/api/users', (req, res) => {
  const { name, email } = req.body;

  const newUser = new User({
    name,
    email
  });

  newUser.save()
    .then(user => {
      res.json({ success: true, user });
    })
    .catch(err => {
      res.status(500).json({ success: false, message: err.message });
    });
});

// Update an existing user
app.put('/api/users', (req, res) => {
  const { id, name, email } = req.body;

  if (!id || !name || !email) {
    return res.status(400).json({ success: false, message: 'ID, name, and email are required.' });
  }

  User.findByIdAndUpdate(id, { name, email }, { new: true })
    .then(updatedUser => {
      if (!updatedUser) {
        return res.status(404).json({ success: false, message: 'User not found.' });
      }
      res.json({ success: true, updatedUser });
    })
    .catch(err => {
      res.status(500).json({ success: false, message: err.message });
    });
});

// Delete a user
app.delete('/api/users', (req, res) => {
  const { id } = req.body;

  User.findByIdAndDelete(id)
    .then(deletedUser => {
      if (!deletedUser) {
        return res.status(404).json({ success: false, message: 'User not found.' });
      }
      res.json({ success: true, message: 'User deleted successfully.' });
    })
    .catch(err => {
      res.status(500).json({ success: false, message: err.message });
    });
});

// Start the server
app.listen(port, () => {
  console.log(`Server running at http://localhost:${port}`);
});
